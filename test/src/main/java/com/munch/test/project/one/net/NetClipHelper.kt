package com.munch.test.project.one.net

import android.content.Context
import android.net.wifi.WifiManager
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.helper.ThreadPoolHelper
import com.munch.pre.lib.helper.file.closeQuietly
import com.munch.pre.lib.log.Logger
import com.munch.pre.lib.log.log
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.Runnable
import java.net.*
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService

/**
 * Create by munch1182 on 2021/4/22 9:35.
 */
class NetClipHelper {

    private val broadcastSendThread = BroadcastSendThread()
    private val broadcastReceiveThread = BroadcastReceiveThread()
    private val multiHelper = MulticastHelper()
    private var listener: ((msg: String, ip: String) -> Unit)? = null
    private val broadcastPool = ThreadPoolHelper.newFixThread()
    private val log = Logger().apply {
        noInfo = true
        tag = "NetClip"
    }

    companion object {
        const val IP_MULTI = "239.0.0.1"

        const val PORT_BROADCAST = 20211
        const val IP_BROADCAST = "255.255.255.255"

    }

    suspend fun start() {
        val msgListener: (msg: ByteArray, ip: String) -> Unit = { msg, ip ->
            val message = ByteHelper.getMessage(msg)
            if (message != null) {
                listener?.invoke(message, ip)
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
        val array = ByteHelper.putMultAddress(IP_MULTI, multiHelper.getPort())
        broadcastPool.execute(broadcastSendThread.update(array))

    }

    fun send(content: String) {
        multiHelper.send(content.toByteArray())
    }

    fun listen(func: (String, String) -> Unit): NetClipHelper {
        listener = func
        return this
    }

    fun stop() {
        multiHelper.stopMulti()
        broadcastSendThread.stopBroadcast()
    }

    fun destroy() {
        stop()
        listener = null
        broadcastPool.shutdownNow()
    }

    private class MulticastHelper {

        private var socket: MulticastSocket? = null
        private var ip: String? = null
        private var port: Int = 0
        private val array = ByteArray(1024)
        private var listener: ((msg: ByteArray, ip: String) -> Unit)? = null
        private val receiverRunnable by lazy {
            Runnable {
                val packet = DatagramPacket(array, array.size)
                while (true) {
                    try {
                        log("开始接收组播")
                        socket?.receive(packet)
                    } catch (e: SocketException) {
                        return@Runnable
                    }
                    log("收到组播")
                    listener?.invoke(array, packet.address.hostName)
                }
            }
        }
        private var receiverThread: ExecutorService? = null
        private var lock: WifiManager.MulticastLock? = null

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
            socket!!.joinGroup(InetAddress.getByName(ip))
            this.port = socket!!.localPort
            this.ip = ip
            log("加入组播: $ip:$port(${this.port})")
            this.listener = listener
            return this
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
            receiverThread?.shutdownNow()
            socket?.closeQuietly()
            unlock()
            socket = null
        }

        fun send(byte: ByteArray) {
            if (byte.isEmpty() || socket == null || ip == null) {
                return
            }
            runBlocking(Dispatchers.IO) {
                socket?.send(
                    DatagramPacket(
                        byte, byte.size, InetAddress.getByName(ip), port
                    )
                )
            }
        }

        fun start() {
            receiverThread?.execute(receiverRunnable)
            val join = ByteHelper.joinMessage()
            socket!!.send(DatagramPacket(join, join.size, InetAddress.getByName(ip), port))
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
            val byteArray = ByteArray(1024)
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
                log("接收到消息：${pack.data.size} byte")
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
        private val mutex = Mutex()
        private var byteArray = byteArrayOf()

        fun stopBroadcast() = runBlocking {
            mutex.withLock { sending = false }
        }

        override fun run() {

            val socket = DatagramSocket()

            val pack = DatagramPacket(
                byteArray, byteArray.size,
                InetAddress.getByName(IP_BROADCAST), PORT_BROADCAST
            )
            runBlocking { mutex.withLock { sending = true } }
            while (true) {
                if (runBlocking { mutex.withLock { !sending } }) {
                    break
                }
                log("发送自己的广播")
                socket.send(pack)
                Thread.sleep(1000L)
            }
            socket.closeQuietly()
        }

        fun update(array: ByteArray): BroadcastSendThread {
            byteArray = array
            return this
        }

    }

    private object ByteHelper {

        const val START = 0xba.toByte()
        const val END = 0xab.toByte()

        const val TYPE_MESSAGE = 0x00.toByte()
        const val TYPE_ADDRESS = 0x01.toByte()

        fun putMultAddress(ip: String, port: Int): ByteArray {
            val bytes = ip.toByteArray()
            val size = bytes.size
            val buffer = ByteBuffer.allocate(1 + 1 + 4 + 1 + size + 1)
            buffer.put(START)
            buffer.put(TYPE_ADDRESS)
            buffer.putInt(port)
            buffer.put(size.toByte())
            buffer.put(bytes)
            buffer.put(END)
            return buffer.array()
        }

        fun getAddress(byteArray: ByteArray): Pair<String, Int>? {
            val buf = ByteBuffer.wrap(byteArray)
            if (buf.get() != START) {
                return null
            }
            if (buf.get() != TYPE_ADDRESS) {
                return null
            }
            val port = buf.int
            val size = buf.get()
            val ipArray = ByteArray(size.toInt())
            buf.get(ipArray)
            val ip = String(ipArray)
            if (buf.get() != END) {
                return null
            }
            return Pair(ip, port)
        }

        fun joinMessage(): ByteArray {
            return putMessage("join")
        }

        fun putMessage(msg: String): ByteArray {
            val bytes = msg.toByteArray()
            val size = bytes.size
            val buffer = ByteBuffer.allocate(1 + 1 + 1 + size + 1)
            buffer.put(START)
            buffer.put(TYPE_MESSAGE)
            buffer.put(size.toByte())
            buffer.put(bytes)
            buffer.put(END)
            return buffer.array()
        }

        fun getMessage(byteArray: ByteArray): String? {
            val buf = ByteBuffer.wrap(byteArray)
            if (buf.get() != START) {
                return null
            }
            if (buf.get() != TYPE_MESSAGE) {
                return null
            }
            val size = buf.get()
            val bytes = ByteArray(size.toInt())
            buf.get(bytes)
            val message = String(bytes)
            if (buf.get() != END) {
                return null
            }
            return message
        }
    }
}