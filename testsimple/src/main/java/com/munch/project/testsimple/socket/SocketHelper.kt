package com.munch.project.testsimple.socket

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import android.os.SystemClock
import androidx.annotation.WorkerThread
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.RequiresPermission
import com.munch.lib.helper.NetStatusHelper
import com.munch.project.testsimple.App
import java.net.*
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2020/12/23 3:46.
 */
class SocketHelper(
    private val application: Application = App.getInstance(),
    owner: LifecycleOwner? = null
) {

    init {
        if (owner != null) {
            NetStatusHelper.getInstance(App.getInstance())
                .apply {
                    setWhenCreate(owner, { _, ca ->
                        ca ?: return@setWhenCreate
                        if (!NetStatusHelper.wifiAvailable(ca) && executor != null) {
                            executor?.shutdown()
                        }
                    }, onDestroy = {
                        unregister()
                    })
                }.register()
        }
    }

    private var executor: ThreadPoolExecutor? = null


    @WorkerThread
    fun scanIpInNet(
        selfIp: String,
        res: (res: ArrayList<String>) -> Unit,
        error: ((error: Exception) -> Unit)? = null
    ) {
        if (!isWifiEnable()) {
            error?.invoke(Exception("wifi不可用"))
            return
        }
        val ipStart = selfIp.substring(0, selfIp.lastIndexOf("."))
        val ipList = arrayListOf<String>()
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
                error?.invoke(Exception("wifi已断开"))
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
    fun isWifiEnable(): Boolean {
        return (application.getSystemService(Context.WIFI_SERVICE) as WifiManager?)?.isWifiEnabled
            ?: false
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
    fun getIpAddress(): String? {
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
}