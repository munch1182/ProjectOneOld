package com.munch.project.testsimple.net

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munch.lib.extend.recyclerview.ExpandableLevelData
import com.munch.lib.helper.ThreadHelper
import okhttp3.internal.closeQuietly
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/1/23 17:18.
 */
class TestSocketBroadcastViewModel : ViewModel() {

    private val socketClientData = MutableLiveData<MutableList<SocketBean>>()
    private var sendBroadcast = false
    private val clientList = mutableListOf<SocketBean>()
    private val clientMap = hashMapOf<String, SocketBean>()

    fun getClientData(): LiveData<MutableList<SocketBean>> = socketClientData

    companion object {
        private const val PORT = 20211
    }

    init {
        /*start()*/
    }

    @WorkerThread
    private fun start() {
        //接受tcp连接
        ThreadHelper.getExecutor().execute {
        }
        //发送udp
        ThreadHelper.newExecutor(1).execute {
            val udpSocket = DatagramSocket(PORT, InetAddress.getByName("255.255.255.255"))
            val buffer = "".toByteArray()
            val packet = DatagramPacket(buffer, buffer.size)

            sendBroadcast = true
            //即使将sendBroadcast更改为false，也只有再发一次消息解除堵塞才有作用
            while (sendBroadcast) {

                udpSocket.send(packet)
                Thread.sleep(5000L)
            }

            if (!sendBroadcast) {
                udpSocket.closeQuietly()
            }
        }
    }

    /**
     * 关闭所有资源
     */
    fun close() {
        ThreadHelper.getExecutor().execute {
            sendBroadcast = false
            /*DatagramSocket()
                .apply {
                    send(DatagramPacket("close".toByteArray(), 10, PORT))
                }
                .closeQuietly()*/
        }
    }

    private fun parseReceiver(packet: DatagramPacket) {
        val ip = "${packet.address}:${packet.port}"

        if (!clientMap.contains(ip)) {
            val clientInfoBean = SocketBean.SocketClientInfoBean(ip, "连接到服务器")
            val clientBean = SocketBean.SocketClientBean(ip, mutableListOf(clientInfoBean))
            clientMap[ip] = clientBean
            clientList.add(clientBean)
        }

        socketClientData.postValue(clientList)
    }

    fun quit(data: SocketBean.SocketClientInfoBean) {

    }

    fun send(data: SocketBean.SocketClientInfoBean) {

    }

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