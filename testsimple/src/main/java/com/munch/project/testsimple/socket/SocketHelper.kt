package com.munch.project.testsimple.socket

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import androidx.annotation.WorkerThread
import com.munch.lib.RequiresPermission
import com.munch.lib.log
import com.munch.project.testsimple.App
import java.io.IOException
import java.net.*
import java.util.*
import kotlin.collections.HashMap

/**
 * 网络变化需要重置本类对象
 *
 * Create by munch1182 on 2020/12/23 3:46.
 */
class SocketHelper(application: Application = App.getInstance()) {
    private val wifiManager by lazy { application.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    private val connectInfo by lazy { wifiManager.connectionInfo }

    companion object {
        private const val PACK_PREFIX = '$'.toByte()
        private const val PACK_TYPE_SEARCH = 0x10.toByte()
        private const val PACK_TYPE_RECEIVE = 0x11.toByte()
    }

    interface ScanIpListener {

        fun scanStart()

        fun scanResult(devices: List<Device>)

        fun scanNewOne(device: Device)

        fun scanError(e: Exception)
    }

    @WorkerThread
    fun scanIpInNet(
        selfIp: String?,
        searchTime: Int = 3,
        scanListener: ScanIpListener
    ) {
        if (searchTime < 1) {
            return
        }
        val socket = DatagramSocket()
        val sendData = ByteArray(1024)
        val receiveData = ByteArray(1024)
        val receivePack = DatagramPacket(receiveData, receiveData.size)
        val endAddress =
            selfIp?.substring(0, selfIp.lastIndexOf("."))?.plus(".255") ?: "255.255.255"
        log(endAddress)
        val sendPack =
            DatagramPacket(sendData, sendData.size, InetAddress.getByName(endAddress), 11)
        val map = HashMap<Device, String>()
        scanListener.scanStart()
        try {
            //搜索多次
            for (i in 0..searchTime) {
                val searchPack = searchPack(i)
                sendPack.data = searchPack
                try {
                    socket.send(sendPack)
                    socket.receive(receivePack)
                    val device = parseRespData(receivePack)
                    log(device)
                    if (device != null) {
                        if (!map.containsKey(device)) {
                            scanListener.scanNewOne(device)
                            map[device] = device.ip
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    log(e.localizedMessage)
                }
            }
            socket.close()
            scanListener.scanResult(map.keys.toList())
        } catch (e: IOException) {
            scanListener.scanError(e)
        } catch (e: SecurityException) {
            scanListener.scanError(e)
        }
    }

    private fun parseRespData(pack: DatagramPacket): Device? {
        if (pack.length < 2) {
            return null
        }
        val data = pack.data
        val offset = pack.offset
        if (data[offset] != PACK_PREFIX && data[offset + 1] != PACK_TYPE_SEARCH) {
            return null
        }
        return Device(pack.address.hostAddress, pack.port)
    }

    /**
     * 生成搜索数据包
     * 格式：$(1) + packType(1) + sendSeq(4) + dataLen(1) + data
     *  packType - 报文类型
     *  sendSeq - 发送序列
     *  dataLen - 数据长度
     *  data - 数据内容
     */
    private fun searchPack(i: Int): ByteArray {
        val data = ByteArray(6)
        data[0] = PACK_PREFIX
        data[1] = PACK_TYPE_RECEIVE
        data[2] = i.toByte()
        data[3] = (i shr 8).toByte()
        data[4] = (i shr 16).toByte()
        data[5] = (i shr 24).toByte()
        return data
    }

    @RequiresPermission("android.permission.ACCESS_WIFI_STATE")
    fun getIpAddressInWifi(): String? {
        val ipAddress = connectInfo.ipAddress
        val addressBytes = byteArrayOf(
            (0xff and ipAddress).toByte(),
            (0xff and (ipAddress shr 8)).toByte(),
            (0xff and (ipAddress shr 16)).toByte(),
            (0xff and (ipAddress shr 24)).toByte()
        )
        return try {
            InetAddress.getByAddress(addressBytes).toString().replace("/", "")
        } catch (e: UnknownHostException) {
            null
        }
    }

    /**
     * 获取网络状态下的ip，包括wifi和4g网
     */
    fun getIpAddressInNet(): String? {
        val networkInterfaces: Enumeration<NetworkInterface>
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces() ?: return null
        } catch (e: SocketException) {
            return null
        }
        for (network in networkInterfaces) {
            for (address in network.inetAddresses) {
                if ((!address.isLoopbackAddress) && (address is Inet4Address)) {
                    return address.hostAddress.toString()
                }
            }
        }
        return null
    }

    data class Device(val ip: String, val port: Int = -1)
}