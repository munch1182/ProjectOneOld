package com.munch.test.project.one.net

import android.content.Context
import android.net.wifi.WifiManager
import androidx.annotation.IntDef
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.helper.ARSHelper
import com.munch.pre.lib.helper.ThreadPoolHelper
import com.munch.pre.lib.helper.file.closeQuietly
import com.munch.pre.lib.log.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.*
import java.nio.ByteBuffer
import java.util.concurrent.ThreadPoolExecutor
import kotlin.coroutines.CoroutineContext

/**
 * 接收广播线程/发送广播线程
 * 接收组播线程
 * 发送组播线程
 *
 * 使用[receivedCallback]通知UI
 * UI调用命令通过[sendMessage]然后回调[receivedCallback]进行识别进行
 * 不允许直接调用，会因为混合调用而混乱
 *
 * Create by munch1182 on 2021/4/22 9:35.
 */
class NetClipHelper private constructor() : CoroutineScope {

    companion object {
        private const val IP_MULTI = "239.21.4.25"

        private const val PORT_BROADCAST = 20211
        private const val IP_BROADCAST = "255.255.255.255"
        private const val LOCK_TAG = "WIFI_NET_CLIP"

        private var INSTANCE: NetClipHelper? = NetClipHelper()

        private val log = Logger().apply {
            tag = "netClip"
            noStack = true
        }

        fun getInstance(): NetClipHelper {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = NetClipHelper()
                    }
                    return INSTANCE!!
                }
            }
            return INSTANCE!!
        }

        fun isConnected(state: Int) = state == ByteHelper.NOTIFY_JOIN.toInt()
        fun isDisconnected(state: Int) = state == ByteHelper.NOTIFY_LEAVE.toInt()
        fun isExit(state: Int) = state == ByteHelper.NOTIFY_EXIT.toInt()
        fun isStart(state: Int) = state == ByteHelper.NOTIFY_START.toInt()

        fun isClosed(state: Int) = state == State.STATE_IDLE || state == State.STATE_DESTROYED
    }


    /**
     * 任务调用线程(组播发送线程) *
     * 广播发送/接收线程 *
     * 组播接收线程 *
     */
    //任务执行线程
    private val mainPool = ThreadPoolHelper.newFixThread(name = "main")
    private val broadcastPool = ThreadPoolHelper.newFixThread(name = "broadcast")
    private val multiReceivePool = ThreadPoolHelper.newFixThread(name = "multi")
    private val dispatcher = FixPoolDispatcher(mainPool)
    private val multi = MultiHelper(multiReceivePool)
    private val broadcastSend by lazy { BroadcastSend(broadcastPool) }
    private val broadcastReceive = BroadcastReceive(broadcastPool)
    private val job = Job()
    override val coroutineContext: CoroutineContext = dispatcher + CoroutineName("NetClip") + job
    var messageListener = object : ARSHelper<((content: String, ip: String) -> Unit)?>() {}
    var notifyListener = object : ARSHelper<((state: Int, ip: String) -> Unit)?>() {}
    var stateListener = object : ARSHelper<((state: Int) -> Unit)?>() {}
    var backgroundListener = object : ARSHelper<((alive: Boolean) -> Unit)?>() {}
    private var keepAlive = false

    @State
    private var state: Int = State.STATE_IDLE
        set(value) {
            field = value
            stateListener.notifyListener { it?.invoke(field) }
        }

    fun isKeepAlive() = runBlocking(coroutineContext) { keepAlive }

    private val receivedCallback: (receive: Received) -> Unit = { r ->
        r.isType(ByteHelper.TYPE_ERROR) { re ->
            if (re == ErrorOutOfLength.get()) {
                messageListener.notifyListener {
                    it?.invoke("too much content", re.ip)
                }
            }
            log.log("error: $re")
        }?.isType(ByteHelper.TYPE_NOTIFY) { re ->
            when (re.sign) {
                ByteHelper.NOTIFY_START -> notifyListener.notifyListener {
                    it?.invoke(re.sign.toInt(), re.ip)
                }
                ByteHelper.NOTIFY_EXIT -> notifyListener.notifyListener {
                    it?.invoke(re.sign.toInt(), re.ip)
                }
                ByteHelper.NOTIFY_JOIN -> notifyListener.notifyListener {
                    it?.invoke(re.sign.toInt(), re.ip)
                }
                ByteHelper.NOTIFY_LEAVE -> notifyListener.notifyListener {
                    it?.invoke(re.sign.toInt(), re.ip)
                }
            }
        }?.isType(ByteHelper.TYPE_COMMAND) { re ->
            when (re.sign) {
                ByteHelper.COMMAND_BROADCAST_SEND_KEEP -> broadcastSend.start()
                ByteHelper.COMMAND_BROADCAST_RECEIVE_KEEP -> broadcastReceive.start()
                ByteHelper.COMMAND_WORK_IN_BACKGROUND -> {
                    runBlocking(coroutineContext) { keepAlive = true }
                    backgroundListener.notifyListener { it?.invoke(true) }
                }
                ByteHelper.COMMAND_NOT_WORK_IN_BACKGROUND -> {
                    runBlocking(coroutineContext) { keepAlive = false }
                    backgroundListener.notifyListener { it?.invoke(false) }
                }
                ByteHelper.COMMAND_BROADCAST_STOP -> {
                    broadcastSend.stop()
                    broadcastReceive.stop()
                }

            }
        }?.isType(ByteHelper.TYPE_MESSAGE) { re ->
            messageListener.notifyListener { it?.invoke(re.getContentString(), re.ip) }
        }
    }

    fun start() {
        launch {
            log.log("start")
            //开始接收广播
            broadcastReceive.apply {
                addressListener = {
                    multi.joinOrStart(it.ip!!, it.port)
                    launch { multi.startReceive(receivedCallback) }
                    state = State.STATE_JOINED
                    //发送join消息
                    notify(ByteHelper.joinMessage())
                }
            }.start()
            state = State.STATE_SCANNING
            broadcastReceive.waitReceive(3000L)
            //指定时间后接收到广播则走回调
            if (broadcastReceive.isReceived()) {
                return@launch
            }
            //否则自己创建端口组播
            multi.joinOrStart(IP_MULTI, 0)
            launch { multi.startReceive(receivedCallback) }
            state = State.STATE_JOINED
            //发送join消息
            notify(ByteHelper.startMessage())
            //然后发送广播
            broadcastSend.updateAddress(multi.getAddress()).start()
        }
    }

    /**
     * 停止所有活动，但没有销毁资源，仍可以调用[start]重新开始
     */
    fun stop() {
        launch {
            multi.send(ByteHelper.leaveMessage())

            multi.leave()
            multi.stop()
            broadcastSend.stop()
            broadcastReceive.stop()
            state = State.STATE_IDLE
        }
    }

    fun sendMessage(message: String) {
        launch {
            when (message) {
                ByteHelper.COMMAND_STR_RECEIVE_KEEP -> multi.send(ByteHelper.receiveKeepMessage())
                ByteHelper.COMMAND_STR_SCAN_KEEP -> multi.send(ByteHelper.scanKeepMessage())
                ByteHelper.COMMAND_STR_STOP -> multi.send(ByteHelper.stopMessage())
                ByteHelper.COMMAND_STR_BACKGROUND -> multi.send(ByteHelper.keepAliveMessage())
                ByteHelper.COMMAND_STR_NOT_BACKGROUND -> multi.send(ByteHelper.notKeepAliveMessage())
                else -> multi.send(ByteHelper.message(message.toByteArray()))
            }
        }
    }

    /**
     * 与[stop]的区别在于，此方法调用后，此类实例必须重建
     *
     * @see getInstance
     */
    fun destroy() {
        launch {
            log.log("called destroy when keep alive = $keepAlive")
            if (!keepAlive) {
                job.cancel()
                mainPool.shutdownNow()
                broadcastPool.shutdownNow()
                multiReceivePool.shutdownNow()
                state = State.STATE_DESTROYED
                INSTANCE = null
                log.log(
                    "NetClipHelper destroyed: job:${job}, pool shutdown:" +
                            "${mainPool.isShutdown && broadcastPool.isShutdown && multiReceivePool.isShutdown}"
                )
            }
        }
    }

    private fun notify(message: ByteArray) {
        launch { multi.send(message) }
    }

    fun hadConnected(): Boolean {
        return state == State.STATE_JOINED
    }

    @IntDef(State.STATE_SCANNING, State.STATE_JOINED, State.STATE_IDLE, State.STATE_DESTROYED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class State {
        companion object {

            const val STATE_SCANNING = 0
            const val STATE_JOINED = 1
            const val STATE_IDLE = 2
            const val STATE_DESTROYED = 3
        }
    }

    private class FixPoolDispatcher(private val ex: ThreadPoolExecutor) : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            ex.execute(block)
        }
    }

    private class MultiHelper(private val pool: ThreadPoolExecutor) {

        private var socket: MulticastSocket? = null
        private val address = Address()
        private var receiveRunning = true
            get() = runBlocking { mutex.withLock { field } }
            set(value) = runBlocking { mutex.withLock { field = value } }
        private val mutex = Mutex()
        private var sendPack: DatagramPacket? = null
        private var lock: WifiManager.MulticastLock? = null

        fun joinOrStart(ip: String, port: Int): MultiHelper {
            if (socket != null) {
                if (!socket!!.isClosed && socket!!.inetAddress.hostName == ip) {
                    log.log("socket not null and same address")
                    return this
                } else {
                    log.log("socket not null:${socket?.inetAddress?.hostName}:${socket?.localPort} ,closed:${socket?.isClosed},  ")
                    socket.closeQuietly()
                    socket = null
                }
            }
            wifiLock()
            socket = MulticastSocket(port)
            socket!!.joinGroup(InetAddress.getByName(ip))
            address.ip = ip
            address.port = socket!!.localPort
            sendPack = DatagramPacket(byteArrayOf(), 0, address.getAddress())
            log.log("join multi cast: $ip:$port(${socket?.localPort})")
            return this
        }

        fun startReceive(listener: (receive: Received) -> Unit) {
            pool.execute {
                val bytes = ByteArray(1024)
                val pack = DatagramPacket(bytes, bytes.size, address.getAddress())
                receiveRunning = true
                log.log("start receive multi cast : $address")
                while (receiveRunning) {
                    log.log("wait multi cast")
                    try {
                        socket?.receive(pack)
                    } catch (e: SocketException) {
                        continue
                    }
                    val message = ByteHelper.getReceived(pack) ?: continue

                    log.log("receive multi cast: $message from: ${pack.address.hostName}:${pack.port}")
                    listener.invoke(message)
                }
                log.log("stop receive multi cast")
                socket?.closeQuietly()
                socket = null
            }
        }

        fun stop() {
            receiveRunning = false
            socket?.closeQuietly()
            unlock()
        }

        /**
         * 此方法应该放在一个类里统一使用，不要分布在不同类里
         */
        fun send(byte: ByteArray) {
            sendPack?.setData(byte) ?: return
            log.log("try to send: ${byte.size} byte")
            socket?.send(sendPack)
        }

        fun getAddress() = address

        fun leave() {
            socket?.leaveGroup(InetAddress.getByName(address.ip))
            log.log("leave multi cast")
        }

        private fun wifiLock() {
            if (lock != null) {
                if (!lock!!.isHeld) {
                    lock!!.acquire()
                }
                return
            }
            val wm =
                BaseApp.getInstance().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            lock = wm.createMulticastLock(LOCK_TAG)
            lock?.acquire()
        }

        private fun unlock() {
            lock?.release()
            lock = null
        }

    }

    private class BroadcastSend(private val pool: ThreadPoolExecutor) : Runnable {

        private var socket: DatagramSocket? = null
        private var running = false
            get() = synchronized(this) { field }
            set(value) = synchronized(this) { field = value }
        private var address: Address? = null

        fun stop() = runBlocking {
            running = false
            delay(1050L)
            socket?.closeQuietly()
            socket = null
        }

        fun start() {
            if (running) {
                return
            }
            pool.execute(this)
        }

        override fun run() {
            running = true
            socket = DatagramSocket(PORT_BROADCAST)
            val array = address?.putMultiAddress()
            if (address == null || array == null) {
                throw IllegalStateException("cannot send broadcast without address")
            }
            val packet = DatagramPacket(
                array,
                array.size,
                Address(IP_BROADCAST, PORT_BROADCAST).getAddress()
            )
            log.log("start send broadcast by self: $address ")
            while (running) {
                try {
                    socket?.send(packet)
                } catch (e: SocketException) {
                    continue
                }
                try {
                    Thread.sleep(1000L)
                } catch (e: InterruptedException) {
                    break
                }
            }
            log.log("stop send broadcast by self")
            socket?.closeQuietly()
            socket = null
        }

        fun updateAddress(address: Address): BroadcastSend {
            this.address = address
            return this
        }
    }

    private class BroadcastReceive(private val pool: ThreadPoolExecutor) : Runnable {

        private var socket: DatagramSocket? = null
        private var running = false
            get() = synchronized(this) { field }
            set(value) = synchronized(this) { field = value }

        private var received = false
        var addressListener: ((address: Address) -> Unit)? = null

        override fun run() {
            running = true
            socket = DatagramSocket(PORT_BROADCAST)
            val array = ByteArray(Address.LENGTH)
            val packet = DatagramPacket(
                array, array.size, InetAddress.getByName(IP_BROADCAST), PORT_BROADCAST
            )
            log.log("try to receive broadcast")
            while (running) {
                try {
                    log.log("start receive broadcast")
                    socket?.receive(packet)
                } catch (e: SocketException) {
                    continue
                }
                val address = Address.getAddress(packet) ?: continue
                log.log("received broadcast : $address")
                received = true
                running = false
                addressListener?.invoke(address)
            }
            socket?.closeQuietly()
            socket = null
            log.log("not received broadcast")
        }

        fun start() {
            if (running) {
                return
            }
            pool.execute(this)
        }

        /**
         * 超过指定时间则关闭接收
         */
        suspend fun waitReceive(timeout: Long) {
            delay(timeout)
            if (!isReceived()) {
                stop()
            }
        }

        fun stop() {
            running = false
            socket?.closeQuietly()
        }

        fun isReceived(): Boolean {
            return received
        }
    }

    private data class Received(val type: Byte, val sign: Byte, val content: ByteArray) {

        lateinit var ip: String
        var port: Int = 0

        fun getContentString() = String(content)

        fun isType(type: Byte, handle: (received: Received) -> Unit): Received? {
            if (type == this.type) {
                handle.invoke(this)
                return null
            }
            return this
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true

            if (other is Received) {
                return type == other.type && sign == other.sign && content.contentEquals(other.content)
            }
            return true
        }

        override fun hashCode(): Int {
            var result = type.toInt()
            result = 31 * result + sign
            result = 31 * result + content.contentHashCode()
            return result
        }

        override fun toString(): String {
            return when (type) {
                ByteHelper.TYPE_MESSAGE -> "Received(${getContentString()})"
                else -> "Received(type:$type,sign:$sign,content:${ByteHelper.format(content)})"
            }
        }
    }

    private object ErrorOutOfLength {

        fun isError(r: Received): Boolean {
            return received.type == r.type && received.sign == r.sign
        }

        fun get() = received

        private val received =
            Received(ByteHelper.TYPE_ERROR, ByteHelper.ERROR_OUT_LENGTH, byteArrayOf())
    }

    private class Address(var ip: String? = null, var port: Int = 0) {

        companion object {
            const val LENGTH = 20

            fun getAddress(pack: DatagramPacket): Address? {
                val message = ByteHelper.getReceived(pack)
                    ?.takeIf { it.type == ByteHelper.TYPE_ADDRESS }
                    ?.content
                    ?: return null
                val wrap = ByteBuffer.wrap(message)
                val port = wrap.int
                val ip = String(message, 4, message.size - 4)
                return Address(ip, port)
            }
        }

        fun putMultiAddress(): ByteArray {
            val ipArray = ip?.toByteArray() ?: throw IllegalStateException("must set ip")
            val buf = ByteBuffer.allocate(ipArray.size + 4)
            buf.putInt(port)
            buf.put(ipArray)
            return ByteHelper.putBytes(ByteHelper.TYPE_ADDRESS, bytes = buf.array())
        }

        fun getAddress(): SocketAddress {
            return InetSocketAddress(ip, port)
        }

        override fun toString(): String {
            return "Address($ip:$port)"
        }
    }

    private object ByteHelper {

        const val START = 0x23.toByte()
        const val END = 0x32.toByte()

        const val MIN_LENGTH = 4

        const val TYPE_MESSAGE = 0x00.toByte()
        const val TYPE_ADDRESS = 0x01.toByte()
        const val TYPE_NOTIFY = 0x02.toByte()
        const val TYPE_COMMAND = 0x03.toByte()
        const val TYPE_ERROR = 0xff.toByte()

        const val ERROR_OUT_LENGTH = 0xfe.toByte()

        //占位字符
        const val BYTE_LOCATE = 0x00.toByte()

        const val NOTIFY_JOIN = 0x00.toByte()
        const val NOTIFY_LEAVE = 0x01.toByte()
        const val NOTIFY_EXIT = 0x02.toByte()
        const val NOTIFY_START = 0x03.toByte()

        const val COMMAND_BROADCAST_SEND_KEEP = 0x01.toByte()
        const val COMMAND_BROADCAST_RECEIVE_KEEP = 0x02.toByte()
        const val COMMAND_BROADCAST_STOP = 0x00.toByte()
        const val COMMAND_CLEAR = 0x03.toByte()
        const val COMMAND_WORK_IN_BACKGROUND = 0x04.toByte()
        const val COMMAND_NOT_WORK_IN_BACKGROUND = 0x05.toByte()

        const val COMMAND_STR_SCAN_KEEP = ":scan"
        const val COMMAND_STR_RECEIVE_KEEP = ":receive"
        const val COMMAND_STR_STOP = ":stop"
        const val COMMAND_STR_BACKGROUND = ":alive"
        const val COMMAND_STR_NOT_BACKGROUND = ":alive not"

        fun joinMessage() = putBytes(TYPE_NOTIFY, NOTIFY_JOIN)
        fun leaveMessage() = putBytes(TYPE_NOTIFY, NOTIFY_LEAVE)
        fun startMessage() = putBytes(TYPE_NOTIFY, NOTIFY_START)
        fun endMessage() = putBytes(TYPE_NOTIFY, NOTIFY_EXIT)

        fun stopMessage() = putBytes(TYPE_COMMAND, COMMAND_BROADCAST_STOP)
        fun scanKeepMessage() = putBytes(TYPE_COMMAND, COMMAND_BROADCAST_SEND_KEEP)
        fun receiveKeepMessage() = putBytes(TYPE_COMMAND, COMMAND_BROADCAST_RECEIVE_KEEP)
        fun clearMessage() = putBytes(TYPE_COMMAND, COMMAND_CLEAR)
        fun keepAliveMessage() = putBytes(TYPE_COMMAND, COMMAND_WORK_IN_BACKGROUND)
        fun notKeepAliveMessage() = putBytes(TYPE_COMMAND, COMMAND_NOT_WORK_IN_BACKGROUND)

        fun message(byteArray: ByteArray) = putBytes(TYPE_MESSAGE, bytes = byteArray)

        fun putBytes(
            type: Byte,
            sign: Byte = BYTE_LOCATE,
            bytes: ByteArray = byteArrayOf()
        ): ByteArray {
            val size = bytes.size
            val buffer = ByteBuffer.allocate(1 + 1 + 1 + 4 + size + 1)
            buffer.put(START)
            buffer.put(type)
            buffer.put(sign)
            buffer.putInt(size)
            if (size > 0) {
                buffer.put(bytes)
            }
            buffer.put(END)
            return buffer.array()
        }

        fun getType(byteArray: ByteArray) =
            if (byteArray.size > MIN_LENGTH) byteArray[1] else TYPE_ERROR

        fun getReceived(pack: DatagramPacket): Received? {
            val buf = ByteBuffer.wrap(pack.data)
            if (buf.get() != START) {
                return null
            }
            val type = buf.get()
            val sign = buf.get()
            val size = buf.int
            if (buf.limit() - buf.position() < size) {
                return ErrorOutOfLength.get()
            }
            val bytes = ByteArray(size)
            buf.get(bytes)
            if (buf.get() != END) {
                return null
            }
            return Received(type, sign, bytes).apply {
                ip = pack.address.hostName
                port = pack.port
            }
        }

        fun format(byteArray: ByteArray) = byteArray.joinToString(
            prefix = "[",
            postfix = "]",
            transform = { String.format("0x%02x", it) })
    }

}