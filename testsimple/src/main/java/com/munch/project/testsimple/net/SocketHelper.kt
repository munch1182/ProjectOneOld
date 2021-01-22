package com.munch.project.testsimple.net

import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.os.SystemClock
import com.munch.lib.closeWhenEnd
import com.munch.lib.helper.ThreadHelper
import com.munch.lib.log
import java.io.Closeable
import java.io.IOException
import java.util.*

/**
 * Create by munch1182 on 2021/1/21 15:00.
 */
class SocketHelper : ISocketHelper {

    private val name = "1231"
    private val socketService by lazy { SocketService(name) }
    private val socketClient by lazy { SocketClient(name) }


    override fun startSocketService() {
        socketService.startService()
    }

    override fun stopSocketService() {
        socketService.stopService()
    }

    override fun clientConnect() {
        socketClient.connect()
    }

    override fun clientSend(msg: String) {
        ThreadHelper.getExecutor().execute {
            socketClient.send(msg.toByteArray())
        }
    }

    override fun clientDisconnect() {
        ThreadHelper.getExecutor().execute {
            socketClient.disconnect()
        }
    }

    override fun closeResource() {
        clientDisconnect()
        stopSocketService()
    }


    /**
     * 服务端：建立服务端-->阻塞接收客户端-->连接后等待消息
     *                                                -->处理消息
     *                                                          -->等待下一次消息
     *                                                          -->断开连接等待下一次连接
     */
    class SocketService(private val socketAddress: String) : Thread(), Closeable {

        private var localServerSocket: LocalServerSocket? = null
        private var start = false

        override fun run() {
            if (localServerSocket != null) {
                return
            }
            localServerSocket = LocalServerSocket(socketAddress)
            while (start) {
                log("s:开始阻塞等待")
                val socket = localServerSocket!!.accept()
                val br = socket.inputStream.bufferedReader()
                while (true) {
                    log("s:等待解析消息")
                    //readLine会堵塞线程
                    val line = br.readLine()
                    if (line.toLowerCase(Locale.ROOT) == "exit") {
                        log("s: 此次解析完毕")
                        break
                    } else {
                        log("s:解析：\"$line\"")
                    }
                }
                log("s:发送回复消息")
                socket.outputStream.run {
                    write("已收到消息\n".toByteArray())
                    write("exit".toByteArray())
                    flush()
                }
                sleep(1000L)
            }
            log("s:退出服务")
        }

        fun startService() {
            if (this.start) {
                return
            }
            this.start = true
            start()
        }

        fun stopService() {
            if (start) {
                log("s:关闭服务")
            }
            start = false
            localServerSocket.closeWhenEnd()
            localServerSocket = null
        }

        override fun close() {
            stopService()
        }

    }

    /**
     * 客户端：连接服务端 --> 发送数据
     *                  -->接收数据
     *                             -->断开连接
     */
    class SocketClient(private val socketName: String) : Closeable {

        private var sender: LocalSocket? = null

        fun connect(): Boolean {
            if (sender == null) {
                sender = LocalSocket()
            }
            log("c:开始连接服务端")
            try {
                sender!!.connect(LocalSocketAddress(socketName))
            } catch (e: IOException) {
                log("c:${e.message}")
            }
            val connected = sender!!.isConnected
            log("c:连接服务端结果: $connected")
            return connected
        }

        fun send(byteArray: ByteArray) {
            try {
                val outputStream = sender?.outputStream ?: return
                log("c:发送消息：${byteArray.decodeToString()}")
                outputStream.write(byteArray)
                outputStream.flush()
                SystemClock.sleep(500L)
                val br = sender?.inputStream?.bufferedReader()
                while (br != null) {
                    //此处应该有超时机制
                    val line = br.readLine()
                    if (line.toLowerCase(Locale.ROOT) == "exit") {
                        log("c:停止等待回复")
                        break
                    } else {
                        log("c:收到回复：$line")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                log(e.message)
            }
        }

        fun disconnect() {
            sender?.outputStream?.run {
                //发送退出消息会让服务端退出解析，转入监听下一次连接，因此结束管道
                write("\n".toByteArray())
                write("exit".toByteArray())
                write("\n".toByteArray())
                flush()
                log("c:断开连接")
            }
            Thread.sleep(500L)
            sender?.closeWhenEnd()
        }

        override fun close() {
            disconnect()
        }
    }
}