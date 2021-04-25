package com.munch.test.project.one.net

import android.content.Context
import android.net.wifi.WifiManager
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.helper.ThreadPoolHelper
import com.munch.pre.lib.helper.file.closeQuietly
import com.munch.pre.lib.log.log
import kotlinx.coroutines.*
import java.net.*
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService

/**
 * 接收广播线程/发送广播线程
 * 接收组播线程
 * 发送组播线程
 * Create by munch1182 on 2021/4/22 9:35.
 */
class NetClipHelperBU private constructor() {

    private val broadcastSendThread = BroadcastSendThread()
    private val broadcastReceiveThread = BroadcastReceiveThread()
    private val multiHelper = MulticastHelper()
    private var notifyListener: ((msg: String, ip: String) -> Unit)? = null
    private var stateListener: ((state: Int, ip: String) -> Unit)? = null
    private val broadcastPool = ThreadPoolHelper.newFixThread(2)
    private var clearListener: (() -> Unit)? = null

    companion object {
        const val IP_MULTI = "239.4.25.904"

        const val PORT_BROADCAST = 20211
        const val IP_BROADCAST = "255.255.255.255"

        val INSTANCE = NetClipHelperBU()

    }

    fun updateFromInstance(): Boolean {
        return multiHelper.getIp() != null
    }

    private fun ByteArray.judgeType(
        type: Byte,
        func: (message: Pair<Byte, ByteArray>) -> Unit
    ): ByteArray? {
        if (ByteHelper.getType(this) == type) {
            val message = ByteHelper.getMessage(this, type)
            if (message != null) {
                func.invoke(message)
            }
            return null
        }
        return this
    }

    suspend fun start() {
        val msgListener: (msg: ByteArray, ip: String) -> Unit = { msg, ip ->

            msg.judgeType(ByteHelper.TYPE_MESSAGE) {
                notifyListener?.invoke(String(it.second), ip)
            }?.judgeType(ByteHelper.TYPE_COMMAND) {
                when (it.first) {
                    ByteHelper.COMMAND_BROADCAST_SEND_KEEP -> {
                        broadcastPool.execute(broadcastSendThread)
                    }
                    ByteHelper.COMMAND_BROADCAST_SEND_STOP -> broadcastSendThread.stopBroadcast()
                    ByteHelper.COMMAND_CLEAR -> clearListener?.invoke()
                }
            }?.judgeType(ByteHelper.TYPE_NOTIFY) {
                when (it.first) {
                    ByteHelper.NOTIFY_JOIN -> {
                        stateListener?.invoke(ByteHelper.NOTIFY_JOIN.toInt(), ip)
                        broadcastSendThread.stopBroadcast()
                    }
                    ByteHelper.NOTIFY_LEAVE -> {
                        stateListener?.invoke(ByteHelper.NOTIFY_LEAVE.toInt(), ip)
                    }
                    ByteHelper.NOTIFY_EXIT -> {
                        stateListener?.invoke(ByteHelper.NOTIFY_EXIT.toInt(), ip)
                        stop()
                    }
                    ByteHelper.NOTIFY_START -> {
                        stateListener?.invoke(ByteHelper.NOTIFY_START.toInt(), ip)
                    }
                }
            }
        }
        broadcastReceiveThread.address = { address ->
            broadcastReceiveThread.stopReceiver()
            //收到组播广播则加入并接收
            multiHelper.joinGroup(address.first, address.second, msgListener).start()
        }
        //接收广播
        broadcastPool.execute(broadcastReceiveThread)
        delay(3000L)
        if (broadcastReceiveThread.received) {
            return
        }
        //3m后未收到则关闭接收
        broadcastReceiveThread.stopReceiver()
        //并创建组播
        multiHelper.joinGroup(IP_MULTI, 0, msgListener).start()
        //并发送自己的广播
        val array = ByteHelper.putMulitAddress(IP_MULTI, multiHelper.getPort())
        broadcastPool.execute(broadcastSendThread.update(array))
    }

    fun send(content: String) {
        when (content.trim().toLowerCase(Locale.getDefault())) {
            ByteHelper.COMMAND_STR_CLEAR -> multiHelper.send(ByteHelper.clearMessage())
            ByteHelper.COMMAND_STR_KEEP -> multiHelper.send(ByteHelper.scanKeepMessage())
            ByteHelper.COMMAND_STR_STOP -> multiHelper.send(ByteHelper.scanStopMessage())
            else -> multiHelper.sendMessage(content.toByteArray())
        }
    }

    fun listen(func: (msg: String, ip: String) -> Unit): NetClipHelperBU {
        notifyListener = func
        return this
    }

    fun state(func: (state: Int, ip: String) -> Unit): NetClipHelperBU {
        stateListener = func
        return this
    }

    fun clear(func: () -> Unit): NetClipHelperBU {
        clearListener = func
        return this
    }

    fun stop() {
        multiHelper.sendLeave()
        broadcastSendThread.stopBroadcast()
        multiHelper.stopMulti()
    }

    fun destroy() {
        stop()
        notifyListener = null
        broadcastPool.shutdownNow()
    }

    fun isConnected(state: Int) = state == ByteHelper.NOTIFY_JOIN.toInt()
    fun isDisconnected(state: Int) = state == ByteHelper.NOTIFY_LEAVE.toInt()
    fun isExit(state: Int) = state == ByteHelper.NOTIFY_EXIT.toInt()
    fun isStart(state: Int) = state == ByteHelper.NOTIFY_START.toInt()

    private class MulticastHelper {

        private var socket: MulticastSocket? = null
        private var ip: String? = null
        private var isCreated = false
        private var port: Int = 0
        private val array = ByteArray(1024)
        private var listener: ((msg: ByteArray, ip: String) -> Unit)? = null
        private val receiverRunnable by lazy {

            object : Runnable {
                var running = true

                override fun run() {
                    val packet =
                        DatagramPacket(array, array.size)
                    running = true
                    while (running) {
                        try {
                            log("开始接收组播")
                            socket?.receive(packet)
                        } catch (e: SocketException) {
                            log(e)
                            return
                        }
                        val msg = ByteHelper.format(array)
                        log("收到组播: $msg")
                        log("来自:${packet.address.hostName}:${packet.port}")
                        listener?.invoke(array, packet.address.hostName)
                    }
                }
            }
        }
        private var receiverThread: ExecutorService? = null
        private var lock: WifiManager.MulticastLock? = null
        private fun DatagramSocket?.send(byte: ByteArray) {
            val datagramPacket = DatagramPacket(
                byte,
                byte.size,
                InetAddress.getByName(ip),
                this@MulticastHelper.port
            )
            this?.send(datagramPacket)
        }


        companion object {
            const val LOCK_TAG = "WIFI_NET_CLIP"
        }

        fun joinGroup(
            ip: String, port: Int, listener: ((msg: ByteArray, ip: String) -> Unit)?
        ): MulticastHelper {
            if (socket != null) {
                return this
            }
            receiverThread = ThreadPoolHelper.newFixThread()
            wifiLock()
            socket = MulticastSocket(port)
            socket!!.timeToLive = 64
            socket!!.joinGroup(InetAddress.getByName(ip))
            this.port = socket!!.localPort
            this.ip = ip
            if (port != 0) {
                isCreated = false
                sendJoin()
            } else {
                isCreated = true
                sendStart()
            }
            log("加入组播: $ip:$port(${this.port})")
            this.listener = listener
            return this
        }

        private fun sendStart() {
            socket.send(ByteHelper.startMessage())
        }

        private fun sendEnd() {
            socket.send(ByteHelper.endMessage())
        }

        fun sendJoin() {
            socket.send(ByteHelper.joinMessage())
        }

        fun sendLeave() {
            if (isCreated) {
                sendEnd()
            } else {
                socket.send(ByteHelper.leaveMessage())
            }
        }

        private fun wifiLock() {
            if (lock != null) {
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

        fun stopMulti() {
            receiverRunnable.running = false
            receiverThread?.shutdownNow()
            socket?.closeQuietly()
            unlock()
            socket = null
        }

        fun sendMessage(byte: ByteArray) {
            if (byte.isEmpty() || socket == null || ip == null) {
                return
            }
            val message = ByteHelper.message(ByteHelper.TYPE_MESSAGE, bytes = byte)
            send(message)
        }

        fun send(message: ByteArray) {
            socket?.send(message)
        }

        fun start() {
            receiverThread?.execute(receiverRunnable)
        }

        fun getPort() = port
        fun getIp() = ip
    }

    private class BroadcastReceiveThread : Runnable {
        var address: ((address: Pair<String, Int>) -> Unit)? = null
        private var socket: DatagramSocket? = null
        var received = false

        override fun run() {
            received = false
            socket = DatagramSocket(PORT_BROADCAST)
            val byteArray = ByteArray(20)
            val pack = DatagramPacket(
                byteArray, byteArray.size,
                InetAddress.getByName(IP_BROADCAST), PORT_BROADCAST
            )

            while (true) {
                try {
                    log("开始尝试接收广播")
                    socket?.receive(pack)
                } catch (e: SocketException) {
                    break
                }
                log("接收到消息：${ByteHelper.format(byteArray)}")
                val get = ByteHelper.getAddress(pack.data) ?: continue
                received = true
                log("接收到广播: ${get.first}:${get.second}")
                address?.invoke(get)
            }
            socket = null
        }

        fun stopReceiver() {
            socket.closeQuietly()
            address = null
            log("不再接收广播")
        }
    }

    private class BroadcastSendThread : Runnable {

        private var sending = true
        private var byteArray = byteArrayOf()

        fun stopBroadcast() {
            sending = false
        }

        override fun run() {

            val socket = DatagramSocket()

            val pack = DatagramPacket(
                byteArray, byteArray.size,
                InetAddress.getByName(IP_BROADCAST), PORT_BROADCAST
            )
            sending = true
            while (true) {
                if (!sending) {
                    break
                }
                log("发送自己的广播")
                socket.send(pack)
                Thread.sleep(1000L)
            }
            socket.closeQuietly()
            log("停止发送自己的广播")
        }

        fun update(array: ByteArray): BroadcastSendThread {
            byteArray = array
            return this
        }

    }

    private object ByteHelper {

        const val START = 0x23.toByte()
        const val END = 0x32.toByte()

        const val TYPE_MESSAGE = 0x00.toByte()
        const val TYPE_ADDRESS = 0x01.toByte()
        const val TYPE_NOTIFY = 0x02.toByte()
        const val TYPE_COMMAND = 0x03.toByte()
        const val TYPE_ERROR = 0xff.toByte()


        const val SIGN_LOCATE = 0x00.toByte()

        const val NOTIFY_JOIN = 0x00.toByte()
        const val NOTIFY_LEAVE = 0x01.toByte()
        const val NOTIFY_EXIT = 0x02.toByte()
        const val NOTIFY_START = 0x03.toByte()

        const val COMMAND_BROADCAST_SEND_KEEP = 0x01.toByte()
        const val COMMAND_BROADCAST_SEND_STOP = 0x02.toByte()
        const val COMMAND_CLEAR = 0x03.toByte()
        const val COMMAND_STR_CLEAR = "cls"
        const val COMMAND_STR_KEEP = "scan"
        const val COMMAND_STR_STOP = "stop"

        fun putMulitAddress(ip: String, port: Int): ByteArray {
            val ipArray = ip.toByteArray()
            val buf = ByteBuffer.allocate(ipArray.size + 4)
            buf.putInt(port)
            buf.put(ipArray)
            return message(TYPE_ADDRESS, bytes = buf.array())
        }

        fun getAddress(byteArray: ByteArray): Pair<String, Int>? {
            val message = getMessage(byteArray, TYPE_ADDRESS)?.second ?: return null
            val wrap = ByteBuffer.wrap(message)
            val port = wrap.int
            val ip = String(message, 4, message.size - 4)
            return Pair(ip, port)
        }

        fun joinMessage() = message(TYPE_NOTIFY, NOTIFY_JOIN)
        fun leaveMessage() = message(TYPE_NOTIFY, NOTIFY_LEAVE)
        fun startMessage() = message(TYPE_NOTIFY, NOTIFY_START)
        fun endMessage() = message(TYPE_NOTIFY, NOTIFY_EXIT)

        fun scanStopMessage() = message(TYPE_COMMAND, COMMAND_BROADCAST_SEND_STOP)
        fun scanKeepMessage() = message(TYPE_COMMAND, COMMAND_BROADCAST_SEND_KEEP)
        fun clearMessage() = message(TYPE_COMMAND, COMMAND_CLEAR)

        fun message(
            type: Byte,
            sign: Byte = SIGN_LOCATE,
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

        fun getType(byteArray: ByteArray): Byte {
            return if (byteArray.isNotEmpty()) {
                byteArray[1]
            } else {
                TYPE_ERROR
            }
        }

        fun getMessage(byteArray: ByteArray, type: Byte): Pair<Byte, ByteArray>? {
            val buf = ByteBuffer.wrap(byteArray)
            if (buf.get() != START) {
                return null
            }
            if (buf.get() != type) {
                return null
            }
            val sign = buf.get()
            val size = buf.int
            if (buf.limit() - buf.position() < size) {
                log("长度错误")
                return null
            }
            val bytes = ByteArray(size)
            buf.get(bytes)
            if (buf.get() != END) {
                return null
            }
            return Pair(sign, bytes)
        }

        fun format(byteArray: ByteArray) = byteArray.joinToString(
            prefix = "[",
            postfix = "]",
            transform = { String.format("0x%02x", it) })
    }
}