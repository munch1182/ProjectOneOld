package com.munch.project.one.net

import android.content.Context
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import com.munch.lib.AppHelper
import com.munch.lib.helper.NetHelper
import com.munch.lib.log.log


/**
 * Created by munch1182 on 2022/5/15 23:04.
 */
@Suppress("DEPRECATION")
class NetViewModel : ViewModel() {

    private val net = NetHelper.instance
    private val wm by lazy { AppHelper.app.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager }

    val isStaticIp: Boolean
        get() = wm?.dhcpInfo?.leaseDuration == 0

    @WorkerThread
    fun queryAvailableAddress(): String? {
        if (net.getCapabilities() == NetworkCapabilities.TRANSPORT_WIFI) {
            return null
        }
        val address = net.getNetAddress() ?: return null
        val ipSuffix = address.subSequence(0, address.lastIndexOf("."))

        repeat(253) {
            val ip = "$ipSuffix.${it + 1}"
            if (ip == address) {
                return@repeat
            }
            try {
                val process =
                    Runtime.getRuntime().exec("ping -c 1 -w 3 $ip")
                if (process.waitFor() != 0) {
                    return ip
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    @WorkerThread
    fun queryNotAvailableAddress(): ArrayList<String>? {
        if (net.getCapabilities() != NetworkCapabilities.TRANSPORT_WIFI) {
            return null
        }
        val address = net.getNetAddress() ?: return null
        val ipSuffix = address.subSequence(0, address.lastIndexOf("."))

        val list = arrayListOf<String>()
        repeat(253) {
            val ip = "$ipSuffix.${it + 1}"
            if (ip == address) {
                return@repeat
            }
            try {
                val cmd = "ping -c 1 -w 3 $ip"
                log("exec $cmd")
                val process = Runtime.getRuntime().exec(cmd)
                val result = process.waitFor()
                if (result == 0) {
                    list.add(ip)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return list
    }
}