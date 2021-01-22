package com.munch.project.testsimple.net

import com.munch.lib.closeWhenEnd
import com.munch.lib.helper.ThreadHelper
import com.munch.lib.log
import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Create by munch1182 on 2021/1/22 9:34.
 */
class SocketUdpHelper : ISocketHelper {

    private val service by lazy { Service(55555) }
    private val client by lazy { Client(55555) }

    override fun startSocketService() {
        service.start()
    }

    override fun stopSocketService() {
        service.close()
    }

    override fun clientConnect() {
        ThreadHelper.getExecutor().execute {
            client.connect()
        }
    }

    override fun clientSend(msg: String) {
        ThreadHelper.getExecutor().execute {
            client.send(msg.toByteArray())
        }
    }

    override fun clientDisconnect() {
        ThreadHelper.getExecutor().execute {
            client.close()
        }
    }

    override fun closeResource() {
        clientDisconnect()
        stopSocketService()
    }


    class Service(private val port: Int = 0) : Thread(), Closeable {

        private var datagramSocket: DatagramSocket? = null
        private var start = false

        override fun run() {
            if (datagramSocket != null) {
                return
            }
            try {
                //1. 创建DatagramSocket实例，并监听端口
                log("s:创建socket，监听 $port 端口")
                datagramSocket = DatagramSocket(port)
                //2. 创建udp数据包
                while (start) {
                    val buffer = ByteArray(512)
                    val packet = DatagramPacket(buffer, buffer.size)
                    //3. 阻塞获取udp数据包
                    log("s:开始阻塞并等待接收数据")
                    datagramSocket!!.receive(packet)
                    log("s:收到消息:${packet.address.hostAddress}:${packet.port}: ${packet.data.decodeToString()}")

                    log("s:回复消息")
                    packet.data = "已收到".toByteArray()
                    datagramSocket!!.send(packet)
                    sleep(500L)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                log("s:${e.message}")
            }
        }

        override fun close() {
            if (start) {
                log("s:关闭服务")
            }
            start = false
            datagramSocket?.closeWhenEnd()
            datagramSocket = null
            interrupt()
        }

        override fun start() {
            if (isAlive) {
                return
            }
            start = true
            super.start()
        }
    }

    class Client(servicePort: Int = 0) : Closeable {

        private var socket: DatagramSocket? = null
        private val packet =
            DatagramPacket(ByteArray(0), 0, InetAddress.getLocalHost(), servicePort)

        fun connect() {
            if (socket != null) {
                return
            }
            log("c:建立客户端")
            socket = DatagramSocket()
        }

        fun send(ba: ByteArray) {
            socket ?: return
            log("c:发送数据到服务端:${ba.decodeToString()}")
            packet.setData(ba, 0, ba.size)
            socket?.send(packet)
            Thread.sleep(500L)
            log("c:等待接收消息")
            packet.data = ByteArray(10)
            socket?.receive(packet)

            log("c:收到消息：${packet.address.hostAddress}:${packet.port}:${packet.data.decodeToString()}")
        }

        override fun close() {
            log("s:关闭客户端")
            socket?.closeWhenEnd()
            socket = null
        }
    }
}