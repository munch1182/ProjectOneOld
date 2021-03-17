package com.munch.project.test.net

import com.munch.lib.helper.closeQuietly
import com.munch.lib.log
import java.io.Closeable
import java.net.*
import java.nio.ByteBuffer
import kotlin.random.Random

/**
 * 1. 申请组播组
 * 2. 通过udp广播发送组播信息，并附上带随机数的创建时间
 * 3. 收到udp广播的应用比较创建时间，创建较晚的关闭自身的组播组，并发送加入组播的信息，
 * 创建较早的将收到的地址加入组播中，并回复加入消息
 * 4. 监听剪切板，发生变化时发出组播广播
 * 5. 当组播组即将关闭时，发出关闭消息，收到关闭消息的应用根据状态决定是否要重启流程
 *
 * Create by munch1182 on 2021/3/17 9:40.
 */
class NetClipHelper {

    private val selfMultiSocket: MulticastSocket by lazy { MulticastSocket() }
    private var selfMultiAddress: MultiAddress? = null
    private var multiThread: MultiThread? = null

    fun start(){

    }

    fun updateMultiAddress(multiAddress: MultiAddress) {
        this.selfMultiAddress = multiAddress
    }

    private fun closeSelfMulti() {
        multiThread?.close()
    }


    private inner class MultiThread : BaseReceiverThread() {

        private val createTime = System.currentTimeMillis() + Random.nextInt(255)

        override fun run() {
            super.run()
            val packet = DatagramPacket(ByteArray(1024), 1024)
            while (receive) {
                try {
                    selfMultiSocket.receive(packet)
                } catch (e: Exception) {
                    e.printStackTrace()
                    continue
                }
            }
        }

        override fun close() {
            pause()
            selfMultiSocket.closeQuietly()
        }
    }

    private inner class UdpReceiverThread : BaseReceiverThread() {

        private var socket: DatagramSocket? = null

        override fun run() {
            super.run()
            socket = DatagramSocket()
            while (receive) {
                if (socket == null) {
                    receive = false
                }
                val packet = DatagramPacket(ByteArray(1024), 1024)
                try {
                    socket?.receive(packet)
                } catch (e: Exception) {
                    e.printStackTrace()
                    continue
                }
                val message = Message.checkOrGet(packet.data) ?: continue
                when (message.type) {
                    Message.TYPE_255 -> {
                        if (message.multiAddress != selfMultiAddress) {
                            if (selfMultiAddress == null) {
                                continue
                            }
                            if (message.multiAddress.createTime > selfMultiAddress!!.createTime) {
                                selfMultiSocket.joinGroup(packet.address)
                            }
                        }
                    }
                }
            }
        }

        override fun close() {
            socket?.closeQuietly()
        }
    }


    private inner class Udp255Thread : Thread() {}

    abstract class BaseReceiverThread : Thread(), Closeable {
        protected var receive = true

        fun pause() {
            receive = false
        }

        fun again() {
            if (receive) {
                return
            }
            receive = true
            start()
        }
    }


    data class Message(
        val key: Int,
        val type: Byte,
        val multiAddress: MultiAddress,
        val sendTime: Long
    ) {

        companion object {

            const val TYPE_255: Byte = 0x01
            const val TYPE_JOINED: Byte = 0x02
            const val TYPE_LEAVE: Byte = 0x03

            private const val KEY = 7130
            private val buf = ByteBuffer.allocate(40)

            fun newJoinedMessage(address: MultiAddress) = newMessage(TYPE_JOINED, address)
            fun newLeavedMessage(address: MultiAddress) = newMessage(TYPE_LEAVE, address)
            fun new255Message(address: MultiAddress) = newMessage(TYPE_255, address)

            private fun newMessage(type: Byte, address: MultiAddress): Message {
                return Message(KEY, type, address, System.currentTimeMillis())
            }

            fun checkOrGet(byteArray: ByteArray): Message? {
                log(buf.limit(), byteArray.size)
                if (buf.limit() != byteArray.size) {
                    return null
                }
                buf.clear()
                buf.put(byteArray)
                val key = buf.int
                if (key != KEY) {
                    return null
                }
                val type = buf.get()
                val addressByte = ByteArray(4)
                buf.get(addressByte)
                val address = MultiAddress(addressByte, buf.int, buf.long)
                return when (type) {
                    TYPE_255 -> new255Message(address)
                    TYPE_JOINED -> newJoinedMessage(address)
                    TYPE_LEAVE -> newLeavedMessage(address)
                    else -> null
                }
            }
        }

        fun toData(): ByteArray {
            buf.clear()
            buf.putInt(key)
            buf.put(type)
            buf.put(multiAddress.multiAddress)
            buf.putInt(multiAddress.port)
            buf.putLong(multiAddress.createTime)
            buf.putLong(sendTime)
            return buf.array()
        }
    }

    data class MultiAddress(val multiAddress: ByteArray, val port: Int, val createTime: Long) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other is MultiAddress) {
                return other.multiAddress.contentEquals(multiAddress) &&
                        other.port == port && other.createTime == createTime
            }
            return false
        }

        override fun hashCode(): Int {
            var result = multiAddress.contentHashCode()
            result = 31 * result + port
            result = 31 * result + createTime.hashCode()
            return result
        }
    }

}