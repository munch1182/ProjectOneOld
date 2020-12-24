package com.munch.project.testsimple.socket

import android.app.Application
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import androidx.annotation.WorkerThread
import com.munch.lib.RequiresPermission
import com.munch.lib.log
import com.munch.project.testsimple.App
import java.net.*
import java.util.*

/**
 * Create by munch1182 on 2020/12/23 3:46.
 */
class SocketHelper(private val application: Application = App.getInstance()) {

    interface ScanIpListener {

        fun scanStart()

        fun scanResult(devices: List<Device>)

        fun scanNewOne(device: Device)

        fun scanError(e: Exception)
    }

    init {
        NetStatus(application).register()
    }

    @WorkerThread
    fun scanIpInNet(
        selfIp: String?,
        searchTime: Int = 3,
        scanListener: ScanIpListener
    ) {

    }

    @RequiresPermission("android.permission.ACCESS_WIFI_STATE")
    fun getIpAddressInWifi(): String? {
        val manager =
            application.getSystemService(Context.WIFI_SERVICE) as WifiManager? ?: return null
        val ipAddress = manager.connectionInfo.ipAddress
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
     * 且无需权限
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

    class NetStatus(context: Context) {
        private val manager =
            (context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)

        private val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                log("onAvailable:$network")
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                log("onLost:$network")
            }
        }

        fun register() {
            manager.registerNetworkCallback(
                NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build(), networkCallback
            )
        }

        fun unregister() {
            manager.unregisterNetworkCallback(networkCallback)
        }
    }
}