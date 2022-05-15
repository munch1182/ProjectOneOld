@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.helper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities.*
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.munch.lib.AppHelper
import com.munch.lib.extend.SingletonHolder
import com.munch.lib.log.Logger
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Created by munch1182 on 2022/5/15 20:10.
 */
@SuppressLint("MissingPermission")
class NetHelper(private val cm: ConnectivityManager? = null) : LiveData<Network?>() {

    companion object : SingletonHolder<NetHelper, Context>({
        NetHelper.objectInstance
            ?: NetHelper(it.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
                .apply { NetHelper.objectInstance = this }
    }) {

        val instance = NetHelper.getInstance(AppHelper.app)

        private var objectInstance: NetHelper? = null
    }

    private val log = Logger("net", enable = false)

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            log.log { "onAvailable ${getName(network)}($network)" }
            postNow()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            log.log { "onLost ${getName(network)}($network)" }
            postNow()
        }
    }

    private fun postNow() {
        postValue(curr)
    }

    override fun onActive() {
        super.onActive()
        postNow()
        register()
    }

    override fun onInactive() {
        super.onInactive()
        unregister()
    }

    /**
     * 注册网络状态方法有数量限制，如果注册过多而不解除注册会抛出错误
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun register(request: NetworkRequest = defNetRequest) {
        cm?.registerNetworkCallback(request, callback)
        log.log { "register NetworkCallback" }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun unregister() {
        cm?.unregisterNetworkCallback(callback)
        log.log { "unregister NetworkCallback" }
    }

    private val netCapabilities: Array<Int> =
        arrayOf(TRANSPORT_WIFI, TRANSPORT_CELLULAR, TRANSPORT_ETHERNET)

    private val defNetRequest: NetworkRequest
        get() = NetworkRequest.Builder()
            .apply { netCapabilities.forEach { addTransportType(it) } }
            .build()

    /**
     * 当前使用的network
     */
    val curr: Network?
        get() = cm?.activeNetwork

    /**
     * 判断network是否能够连接网络
     *
     * @see netCapabilities
     */
    fun isConnected(network: Network? = curr): Boolean {
        network ?: return false
        val capabilities = cm?.getNetworkCapabilities(network) ?: return false
        if (!capabilities.hasCapability(NET_CAPABILITY_VALIDATED)) {
            return false
        }
        return netCapabilities.any { capabilities.hasTransport(it) }
    }

    /**
     * 获取netWork的名称(自定义)
     */
    fun getName(network: Network? = curr): String? {
        network ?: return null
        val capabilities = cm?.getNetworkCapabilities(network) ?: return null
        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> "WIFI"
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> "CELLULAR"
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> "ETHERNET"
            capabilities.hasTransport(TRANSPORT_BLUETOOTH) -> "BLUETOOTH"
            capabilities.hasTransport(TRANSPORT_USB) -> "USB"
            capabilities.hasTransport(TRANSPORT_VPN) -> "VPN"
            capabilities.hasTransport(TRANSPORT_LOWPAN) -> "LOWPAN"
            capabilities.hasTransport(TRANSPORT_WIFI_AWARE) -> "WIFI AWARE"
            else -> null
        }
    }

    /**
     * 获取network的联网能力
     */
    fun getCapabilities(network: Network? = curr): Int? {
        network ?: return null
        val capabilities = cm?.getNetworkCapabilities(network) ?: return null
        when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> return TRANSPORT_WIFI
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> return TRANSPORT_CELLULAR
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> return TRANSPORT_ETHERNET
            capabilities.hasTransport(TRANSPORT_BLUETOOTH) -> return TRANSPORT_BLUETOOTH
            capabilities.hasTransport(TRANSPORT_VPN) -> return TRANSPORT_VPN
        }
        if (capabilities.hasTransport(TRANSPORT_WIFI_AWARE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return TRANSPORT_WIFI_AWARE
        }
        if (capabilities.hasTransport(TRANSPORT_USB)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return TRANSPORT_USB
        }
        if (capabilities.hasTransport(TRANSPORT_LOWPAN)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) return TRANSPORT_LOWPAN
        }
        return null
    }

    fun getLinkProperties(network: Network?) = cm?.getLinkProperties(network)


    /**
     * 获取网络地址
     */
    @WorkerThread
    fun getNetAddress(): String? {
        try {
            for (enumeration in NetworkInterface.getNetworkInterfaces()) {
                for (address in enumeration.inetAddresses) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

inline fun Network.getName(context: Context = AppHelper.app) =
    NetHelper.getInstance(context).getName(this)

inline fun Network.isConnected(context: Context = AppHelper.app) =
    NetHelper.getInstance(context).isConnected(this)

inline fun Network.desc(context: Context = AppHelper.app) = "${getName(context)}($this)"

inline fun Network.getLinkProperties(context: Context = AppHelper.app) =
    NetHelper.getInstance(context).getLinkProperties(this)


