package com.munch.project.testsimple.socket

import android.app.Application
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.SystemClock
import androidx.annotation.WorkerThread
import com.munch.lib.RequiresPermission
import com.munch.lib.log
import com.munch.project.testsimple.App
import java.net.*
import java.util.*
import java.util.concurrent.*

/**
 * Create by munch1182 on 2020/12/23 3:46.
 */
class SocketHelper(private val application: Application = App.getInstance()) {

    init {
        NetStatus(application).register { connect ->
            if (!connect && executor != null) {
                executor?.shutdown()
            }
            listener?.invoke(connect)
        }
    }

    private var executor: ThreadPoolExecutor? = null
    private var listener: ((connect: Boolean) -> Unit)? = null

    fun listenerWifi(func: (connect: Boolean) -> Unit): SocketHelper {
        listener = func
        return this
    }

    @WorkerThread
    fun scanIpInNet(
        selfIp: String,
        res: (res: ArrayList<String>) -> Unit,
        error: ((error: Exception) -> Unit)? = null
    ) {
        val ipStart = selfIp.substring(0, selfIp.lastIndexOf("."))
        val ipList = arrayListOf<String>()
        Executors.newCachedThreadPool()
        executor = ThreadPoolExecutor(1, 255, 60L, TimeUnit.SECONDS, ArrayBlockingQueue(1))
        var ip: String
        for (i in 1..255) {
            ip = ipStart.plus(".").plus(i)
            if (ip == selfIp) {
                continue
            }
            try {
                executor?.execute(IpRunnable(ip, ipList))
            } catch (e: RejectedExecutionException) {
                error?.invoke(Exception())
                //当连接断开后触发NetStatus.register时此处会结束
                break
            }
        }
        executor?.shutdown()
        while (true) {
            SystemClock.sleep(50L)
            if (executor?.isTerminated == true) {
                res.invoke(ipList)
                return
            }
        }
    }

    private class IpRunnable(
        private val ip: String,
        private val ips: ArrayList<String>
    ) : Runnable {
        override fun run() {
            var process: Process? = null
            try {
                //-c 1为发送的次数，-w 表示发送后等待响应的时间
                process = Runtime.getRuntime().exec("ping -c 1 -w 3 $ip")
                val res = process.waitFor()
                if (res == 0) {
                    ips.add(ip)
                } /*else {
                    LogLog.simple().log("$ip:$res")
                }*/
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                process?.destroy()
            }
        }
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

    class NetStatus(context: Context) {
        private val manager =
            (context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)
        private var listener: ((connect: Boolean) -> Unit)? = null

        private val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                log("onAvailable:$network")
                listener?.invoke(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                log("onLost:$network")
                listener?.invoke(false)
            }
        }

        @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
        fun register(listener: ((connect: Boolean) -> Unit)? = null) {
            this.listener = listener
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