package com.munch.test.project.one.net

import java.net.*
import java.nio.ByteBuffer
import java.util.*
import kotlin.concurrent.thread

/**
 * Create by munch1182 on 2021/4/23 15:09.
 */
const val IP_MULTI = "239.0.0.1"

const val PORT_BROADCAST = 20211
const val IP_BROADCAST = "255.255.255.255"


fun main() {
    //ip需要保持在局域网
    println(Inet4Address.getLocalHost().hostAddress)

    val address = startBroadcastReceiver() ?: return

    val port = address.second
    val ip = address.first
    println("join: $ip:$port")
    val socket = MulticastSocket(port)
    socket.timeToLive = 64
    socket.joinGroup(InetAddress.getByName(ip))
    val joinMessage = ByteHelper.joinMessage()
    socket.send(DatagramPacket(joinMessage, joinMessage.size, InetAddress.getByName(ip), port))

    startReceiver(socket, ip, port)

    startInput(socket, ip, port)
}

fun startInput(socket: MulticastSocket, ip: String, port: Int) {
    val scanner = Scanner(System.`in`)

    while (scanner.hasNext()) {
        val nextLine = scanner.nextLine()
        val array = ByteHelper.message(ByteHelper.TYPE_MESSAGE, bytes = nextLine.toByteArray())
        socket.send(DatagramPacket(array, array.size, InetAddress.getByName(ip), port))
    }
}


fun startReceiver(socket: MulticastSocket, ip: String, port: Int) {
    thread {
        val array = ByteArray(1024)
        val pack = DatagramPacket(array, array.size, InetAddress.getByName(ip), port)
        while (true) {
            try {
                println("wait multicast")
                socket.receive(pack)
            } catch (e: SocketException) {
                println("error: ${e.message}")
                continue
            }
            val message = ByteHelper.getMessage(array, ByteHelper.TYPE_MESSAGE)
            if (message?.second != null) {
                println("===============")
                println(String(message.second))
                println("===============")
            }
        }
    }
}

fun startBroadcastReceiver(): Pair<String, Int>? {
    val socket = DatagramSocket(PORT_BROADCAST)
    val byteArray = ByteArray(20)
    val pack = DatagramPacket(
        byteArray, byteArray.size,
        InetAddress.getByName(IP_BROADCAST), PORT_BROADCAST
    )

    var address: Pair<String, Int>? = null
    while (true) {
        try {
            println("wait address")
            socket.receive(pack)
        } catch (e: SocketException) {
            break
        }
        println("receiver：${ByteHelper.format(byteArray)}")
        println("from：${pack.address.hostName}:${pack.port}")
        address = ByteHelper.getAddress(pack.data) ?: continue
        break
    }
    socket.close()
    return address
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

    fun message(
        type: Byte,
        sign: Byte = SIGN_LOCATE,
        bytes: ByteArray = byteArrayOf()
    ): ByteArray {
        val size = bytes.size
        val buffer = ByteBuffer.allocate(1 + 1 + 1 + 1 + size + 1)
        buffer.put(START)
        buffer.put(type)
        buffer.put(sign)
        buffer.put(size.toByte())
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
        val size = buf.get()
        if (buf.limit() - buf.position() < size) {
            return null
        }
        val bytes = ByteArray(size.toInt())
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
