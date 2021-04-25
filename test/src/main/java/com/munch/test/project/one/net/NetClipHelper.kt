package com.munch.test.project.one.net

import androidx.annotation.IntDef
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
        const val IP_MULTI = "239.4.25.904"

        const val PORT_BROADCAST = 20211
        const val IP_BROADCAST = "255.255.255.255"

        val INSTANCE = NetClipHelper()

        val log = Logger().apply { tag = "netClip" }
    }

    /**
     * 任务调用线程(组播发送线程) *
     * 广播发送/接收线程 *
     * 组播接收线程 *
     */
    private val pool = ThreadPoolHelper.newFixThread(3)
    private val dispatcher = FixPoolDispatcher(pool)
    private val multi = MultiHelper()
    private val broadcastSend by lazy { BroadcastSend(pool) }
    private val broadcastReceive = BroadcastReceive(pool)
    private val job = Job()
    override val coroutineContext: CoroutineContext = dispatcher + CoroutineName("NetClip") + job
    var messageListener: ((content: String, ip: String) -> Unit)? = null
    var notifyListener: ((state: Int, ip: String) -> Unit)? = null
    var stateListener: ((state: Int) -> Unit)? = null
    private var keepAlive = false

    @State
    private var state: Int = State.STATE_IDLE
        set(value) {
            field = value
            stateListener?.invoke(field)
        }

    fun isKeepAlive() = runBlocking(coroutineContext) { keepAlive }

    private val receivedCallback: (receive: Received) -> Unit = { r ->
        r.isType(ByteHelper.TYPE_ERROR) {
            if (it == ErrorOutOfLength.get()) {
                messageListener?.invoke("too much content", it.ip)
            }
            log.log("error: $it")
        }?.isType(ByteHelper.TYPE_NOTIFY) {
            when (it.sign) {
                ByteHelper.NOTIFY_START -> notifyListener?.invoke(it.sign.toInt(), it.ip)
                ByteHelper.NOTIFY_EXIT -> notifyListener?.invoke(it.sign.toInt(), it.ip)
                ByteHelper.NOTIFY_JOIN -> notifyListener?.invoke(it.sign.toInt(), it.ip)
                ByteHelper.NOTIFY_LEAVE -> notifyListener?.invoke(it.sign.toInt(), it.ip)
            }
        }?.isType(ByteHelper.TYPE_COMMAND) {
            when (it.getContentString()) {
                ByteHelper.COMMAND_STR_SCAN_KEEP -> broadcastSend.start()
                ByteHelper.COMMAND_STR_RECEIVE_KEEP -> broadcastReceive.start()
                ByteHelper.COMMAND_STR_BACKGROUND -> runBlocking(coroutineContext) {
                    keepAlive = true
                }
                ByteHelper.COMMAND_STR_STOP -> {
                    broadcastSend.stop()
                    broadcastReceive.stop()
                }

            }
        }?.isType(ByteHelper.TYPE_MESSAGE) {
            messageListener?.invoke(it.getContentString(), it.ip)
        }
    }

    fun start() {
        launch {
            //开始接收广播
            broadcastReceive.apply {
                addressListener = {
                    multi.joinOrStart(it.ip!!, it.port).startReceive(receivedCallback)
                    state = State.STATE_JOINED

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
            multi.joinOrStart(IP_MULTI, 0).startReceive(receivedCallback)
            state = State.STATE_JOINED

            notify(ByteHelper.startMessage())
            //然后发送广播
            broadcastSend.updateAddress(multi.getAddress()).start()
        }
    }

    fun stop() {
        launch {
            multi.stop()
            broadcastSend.stop()
            broadcastReceive.stop()
        }
    }

    fun leave() {
        launch {
            multi.leave()
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
                else -> multi.send(message.toByteArray())
            }
        }
    }

    fun destroy() {
        log.log("called destroy when keep alive = $keepAlive")
        launch {
            if (!keepAlive) {
                job.cancel()
                pool.shutdownNow()
                state = State.STATE_DESTROYED
                log.log("NetClipHelper destroyed")
            }
        }
    }

    fun isConnected(state: Int) = state == ByteHelper.NOTIFY_JOIN.toInt()
    fun isDisconnected(state: Int) = state == ByteHelper.NOTIFY_LEAVE.toInt()
    fun isExit(state: Int) = state == ByteHelper.NOTIFY_EXIT.toInt()
    fun isStart(state: Int) = state == ByteHelper.NOTIFY_START.toInt()

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

    private class MultiHelper {

        private var socket: MulticastSocket? = null
        private val address = Address()
        private var receiveRunning = true
            get() = runBlocking { mutex.withLock { false } }
            set(value) = runBlocking { mutex.withLock { field = value } }
        private val mutex = Mutex()
        private var sendPack: DatagramPacket? = null

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
            socket = MulticastSocket(port)
            socket!!.joinGroup(InetAddress.getByName(ip))
            address.ip = ip
            address.port = socket!!.localPort
            sendPack = DatagramPacket(byteArrayOf(), 0, address.getAddress())
            log.log("join multicast: $ip:$port(${socket?.localPort})")
            return this
        }

        fun startReceive(listener: (receive: Received) -> Unit) {
            val bytes = ByteArray(1024)
            val pack = DatagramPacket(bytes, bytes.size, address.getAddress())
            while (receiveRunning) {
                try {
                    log.log("wait multi cast")
                    socket?.receive(pack)
                } catch (e: SocketException) {
                    continue
                }
                val message = ByteHelper.getReceived(pack) ?: continue

                log.log("receive multi cast: $message")
                listener.invoke(message)
            }
        }

        fun stop() {
            receiveRunning = false
            socket?.closeQuietly()
        }

        /**
         * 此方法应该放在一个类里统一使用，不要分布在不同类里
         */
        fun send(byte: ByteArray) {
            sendPack?.setData(byte) ?: return
            socket?.send(sendPack)
        }

        fun getAddress() = address

        fun leave() {
            socket?.leaveGroup(InetAddress.getByName(address.ip))
        }

    }

    private class BroadcastSend(private val pool: ThreadPoolExecutor) : Runnable {

        private var socket: DatagramSocket? = null
        private var running = true
            get() = runBlocking { mutex.withLock { false } }
            set(value) = runBlocking { mutex.withLock { field = value } }
        private var mutex = Mutex()
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
            val array = Address(IP_BROADCAST, PORT_BROADCAST).putMultiAddress()
            if (address == null) {
                throw IllegalStateException("cannot send broadcast without address")
            }
            val packet = DatagramPacket(array, array.size, address?.getAddress())
            log.log("start send broadcast by self: $address ")
            while (running) {
                try {
                    socket?.send(packet)
                } catch (e: SocketException) {
                    continue
                }
                Thread.sleep(1000L)
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
        private var running = true
            get() = runBlocking { mutex.withLock { field } }
            set(value) = runBlocking { mutex.withLock { field = value } }

        private var received = false
        private var mutex = Mutex()
        var addressListener: ((address: Address) -> Unit)? = null

        override fun run() {
            running = true
            socket = DatagramSocket(PORT_BROADCAST)
            val array = ByteArray(Address.LENGTH)
            val packet = DatagramPacket(
                array, array.size, InetAddress.getByName(IP_BROADCAST), PORT_BROADCAST
            )
            while (running) {
                try {
                    log.log("start receive broadcast")
                    socket?.receive(packet)
                } catch (e: SocketException) {
                    continue
                }
                val address = Address.getAddress(packet) ?: continue
                log.log("receive broadcast : $address")
                received = true
                running = false
                addressListener?.invoke(address)
            }
            socket?.closeQuietly()
            socket = null
            log.log("not receive broadcast")
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

        fun isReceived() = received
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

        const val COMMAND_STR_SCAN_KEEP = ":scan"
        const val COMMAND_STR_RECEIVE_KEEP = ":receive"
        const val COMMAND_STR_STOP = ":stop"
        const val COMMAND_STR_BACKGROUND = ":alive"

        fun joinMessage() = putBytes(TYPE_NOTIFY, NOTIFY_JOIN)
        fun leaveMessage() = putBytes(TYPE_NOTIFY, NOTIFY_LEAVE)
        fun startMessage() = putBytes(TYPE_NOTIFY, NOTIFY_START)
        fun endMessage() = putBytes(TYPE_NOTIFY, NOTIFY_EXIT)

        fun stopMessage() = putBytes(TYPE_COMMAND, COMMAND_BROADCAST_STOP)
        fun scanKeepMessage() = putBytes(TYPE_COMMAND, COMMAND_BROADCAST_SEND_KEEP)
        fun receiveKeepMessage() = putBytes(TYPE_COMMAND, COMMAND_BROADCAST_RECEIVE_KEEP)
        fun clearMessage() = putBytes(TYPE_COMMAND, COMMAND_CLEAR)
        fun keepAliveMessage() = putBytes(TYPE_COMMAND, COMMAND_WORK_IN_BACKGROUND)

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