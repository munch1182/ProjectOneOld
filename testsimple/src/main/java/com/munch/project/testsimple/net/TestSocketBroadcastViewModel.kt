package com.munch.project.testsimple.net

import android.net.LocalServerSocket
import android.net.LocalSocket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munch.lib.extend.recyclerview.ExpandableLevelData
import com.munch.lib.helper.ProcedureLog
import com.munch.lib.helper.ThreadHelper
import okhttp3.internal.closeQuietly
import java.io.BufferedWriter
import java.io.Closeable
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import kotlin.random.Random

/**
 *
 * 两个同一协议的未知设备的局域网连接
 *
 * 设备A发送udp局域网广播，带有标识
 * 设备B接收到局域网广播，建立tcp并回复tcp端口
 * 设备A连接到设备B
 * Create by munch1182 on 2021/1/23 17:18.
 */
class TestSocketBroadcastViewModel : ViewModel() {

    private val socketClientData = MutableLiveData<MutableList<SocketBean>>()
    fun getClientData(): LiveData<MutableList<SocketBean>> = socketClientData
    private var isConnected = false
    private var writer: BufferedWriter? = null
    private val closeable = arrayListOf<Closeable?>()
    private val log = ProcedureLog("TestSocketBroadcast".plus(Random.nextInt(10)), this::class.java)

    /**
     * 发送udp的端口，用来过滤
     */
    private var sendPort = -1
    private var tcpName: String? = null

    /*init {
         startSearch()
     }*/

    fun startSearch() {
        if (!isConnected) {
            close()
            log.start()
            listenUdpBroadcast()
            sendUdpBroadcast()
        }
    }

    fun close() {
        isConnected = false
        //关闭所有能关闭的流
        closeable.forEach {
            it?.closeQuietly()
        }
        closeable.clear()
        log.end()
    }

    private fun listenUdpBroadcast() {
        if (isConnected) {
            return
        }
        ThreadHelper.getExecutor().execute {
            var socket: DatagramSocket? = DatagramSocket(Protocol.BROADCAST_PORT)
            val buffer = ByteBuffer.allocate(128)
            val packet = DatagramPacket(
                buffer.array(),
                buffer.position()
            )
            closeable.add(socket)
            log.step("开始监听udp")
            while (!isConnected) {
                try {
                    socket?.receive(packet)
                } catch (e: IOException) {
                    return@execute
                }
                val port = packet.port
                //过滤掉自身发送的udp
                if (sendPort != -1 && port == sendPort) {
                    continue
                }
                if (parseUdpReceiver(socket, packet, port)) {
                    //并启动自身的tcp服务
                    startTcpService()
                    //并关闭udp广播
                    closeAndRemove(socket)
                    socket = null
                    isConnected = true
                    log.step("关闭自身的udp广播和监听")
                }
                Thread.sleep(1000L)
            }
            log.step("udp监听关闭")
        }
    }

    private fun parseUdpReceiver(
        socket: DatagramSocket?,
        packet: DatagramPacket,
        port: Int
    ): Boolean {

        val wrap = ByteBuffer.wrap(packet.data)
        val token = wrap.int

        log.step("接收到${packet.address}:${port}:token=${token}")

        //无法匹配token，则继续监听
        if (token != Protocol.TOKEN) {
            return true
        }
        //是否已经建立了tcp端口
        val isService = wrap.char == Protocol.SERVICE_Y

        //特殊情况，即使是收到消息的应用也不建立tcp
        if (isService && createTcp()) {
            wrap.clear()
            //发送格式：第一位是int，为token
            wrap.putInt(Protocol.TOKEN)
            //发送格式，第二位是char，用以标识是否是由我方建立tcp
            wrap.putChar(Protocol.SERVICE_Y)
            //回复udp
            socket?.send(packet)
            return true
        }

        log.step("接收到${packet.address}:${port}:isService=${wrap.char}")
        //获取已建立的tcp端口
        if (isService) {
            val array = ByteArray(128)
            wrap.get(array)
            tcpName = array.decodeToString()
            log.step("接收到${packet.address}:${port}:receiverTcpName=${tcpName}")
            //关闭udp，准备连接tcp
            return false
            //没有tcp端口则由我方建立tcp端口，如果不想由此应用建立tcp，则发送Protocol.SERVICE_N即可
        } else {
            wrap.clear()
            //发送格式：第一位是int，为token
            wrap.putInt(Protocol.TOKEN)
            //发送格式，第二位是char，用以标识是否是由我方建立tcp
            wrap.putChar(Protocol.SERVICE_Y)
            //发送格式：第三位是byteArray128位，为tcpName
            val byteArray = ByteArray(128)
            val currentTcpName = newTcpName()
            currentTcpName.toByteArray().forEachIndexed { index, byte ->
                byteArray[index] = byte
            }
            wrap.put(byteArray)
            packet.data = wrap.array()
            log.step("回复: tcpName=$currentTcpName")
            //回复udp
            socket?.send(packet)
            //关闭udp，准备建立tcp
            return false
        }
    }

    private fun createTcp(): Boolean {
        return false
    }

    private fun newTcpName(): String {
        return "clip".plus(Random.nextInt(999))

    }

    private fun startTcpService() {
        tcpName ?: return
        ThreadHelper.getExecutor().execute {
            var service: LocalServerSocket?
            log.step("开始tcp：tcpName=${tcpName}")
            try {
                service = LocalServerSocket(tcpName)
            } catch (e: IOException) {
                log.step("tcp失败: ${e.message}")
                return@execute
            }
            closeable.add(service)
            while (isConnected) {
                val socket: LocalSocket?
                try {
                    socket = service?.accept()
                } catch (e: IOException) {
                    return@execute
                }
                closeable.add(socket)
                val isbr = socket?.inputStream?.bufferedReader() ?: break
                closeable.add(isbr)
                writer = socket.outputStream?.bufferedWriter() ?: break
                closeable.add(writer)
                while (true) {
                    val line = isbr.readLine()
                    //退出
                    if (line == Protocol.EXIT) {
                        isConnected = false
                        closeAndRemove(writer)
                        writer = null
                        closeAndRemove(isbr)
                        service?.closeQuietly()
                        closeable.remove(service)
                        service = null
                        closeAndRemove(socket)
                        log.step("tcp收到:${line},停止接收")
                        break
                    } else {
                        update(line)
                        log.step("tcp收到: $line")
                    }
                }
            }
            log.step("tcp退出")
        }
    }

    private fun closeAndRemove(close: Closeable?) {
        close ?: return
        close.closeQuietly()
        closeable.remove(close)
    }

    private fun sendUdpBroadcast() {
        if (isConnected) {
            return
        }
        ThreadHelper.getExecutor().execute {
            val socket = DatagramSocket()
            val buffer = ByteBuffer.allocate(128)
            buffer.putInt(Protocol.TOKEN)
            val packet = DatagramPacket(
                buffer.array(),
                buffer.position(),
                InetAddress.getByName("255.255.255.255"),
                Protocol.BROADCAST_PORT
            )
            closeable.add(socket)
            sendPort = socket.localPort
            while (!isConnected) {
                try {
                    socket.send(packet)
                } catch (e: IOException) {
                    return@execute
                }
                log.step("发送udp255")
                Thread.sleep(5000L)
            }

            log.step("停止发送udp255")
            closeAndRemove(socket)
        }
    }

    private fun update(line: String) {

    }


    //<editor-fold desc="view方法">
    fun quit(data: SocketBean.SocketClientInfoBean) {
    }

    fun send(data: SocketBean.SocketClientInfoBean) {
    }
    //</editor-fold>


    //<editor-fold desc="实现">
    object Protocol {

        const val EXIT = "exit"

        const val TOKEN = 20211

        const val SERVICE_Y = 'Y'
        const val SERVICE_N = 'N'

        const val BROADCAST_PORT = 20211
    }

    class Service {

    }

    class Client {

    }
    //</editor-fold>

    sealed class SocketBean : ExpandableLevelData {

        data class SocketClientBean(
            val ip: String,
            val list: MutableList<ExpandableLevelData>? = null,
            var isExpand: Boolean = false
        ) : SocketBean() {

            override fun expandLevel(): Int {
                return 0
            }

            override fun getExpandableData(): MutableList<ExpandableLevelData>? {
                return list
            }

            override fun toString(): String {
                return "SocketClientBean(ip=$ip,  isExpand=$isExpand)"
            }

            companion object {

                fun newInstance(): SocketClientBean {
                    val ip = "192.168.1.${Random.nextInt(256)}"
                    return SocketClientBean(ip, mutableListOf(SocketClientInfoBean.newInstance(ip)))
                }
            }
        }

        data class SocketClientInfoBean(val ip: String, var msg: String) : SocketBean() {
            override fun expandLevel(): Int {
                return 1
            }

            companion object {

                fun newInstance(ip: String): SocketClientInfoBean {
                    return SocketClientInfoBean(ip, "收到信息：aaaaaa\nbbbbb\nccccc\nddddd")
                }
            }
        }
    }
}