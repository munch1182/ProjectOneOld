@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import androidx.annotation.RequiresPermission
import com.munch.lib.app.AppHelper
import com.munch.lib.base.SingletonHolder
import com.munch.lib.helper.ARSHelper
import com.munch.lib.log.Logger
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 * Create by munch1182 on 2020/12/28 13:51.
 */
class NetHelper private constructor(context: Context) :
    ARSHelper<(available: Boolean, capabilities: NetworkCapabilities?) -> Unit> {

    private val manager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    private val wifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
    }
    private val list =
        mutableListOf<(available: Boolean, capabilities: NetworkCapabilities?) -> Unit>()
    override val arrays: MutableList<(available: Boolean, capabilities: NetworkCapabilities?) -> Unit>
        get() = list
    private val logHelper = Logger().apply {
        tag = "net-status-helper"
        noStack = true
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    private constructor() : this((AppHelper.app.applicationContext))

    companion object : SingletonHolder<NetHelper, Context>({ NetHelper(it) }) {

        fun getInstance() = getInstance(AppHelper.app)
    }

    /**
     * 获取网络状态下的ip
     */
    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    fun getIpAddress(): String? {
        val networkInterfaces: Enumeration<NetworkInterface>
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces() ?: return null
        } catch (e: SocketException) {
            e.printStackTrace()
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

    private var transportType = intArrayOf()

    val currentNet: NetworkCapabilities?
        @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
        get() = manager?.getNetworkCapabilities(manager.activeNetwork)

    val allNet: Array<Network>
        @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
        get() = manager?.allNetworks ?: arrayOf()

    /**
     * wifi是否可用；当wifi正在关闭、已关闭、正在打开但未完全打开时不可用
     */
    val wifiAvailable: Boolean
        @RequiresPermission("android.permission.ACCESS_WIFI_STATE")
        get() {
            val state = wifiManager?.wifiState ?: return false
            return when (state) {
                WifiManager.WIFI_STATE_DISABLED, WifiManager.WIFI_STATE_DISABLING, WifiManager.WIFI_STATE_ENABLING -> false
                WifiManager.WIFI_STATE_ENABLED -> true
                else -> false
            }
        }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    fun getCapabilities(network: Network) = manager?.getNetworkCapabilities(network)

    /**
     * 检查wifi状态是否和[enable]一致
     */
    @RequiresPermission("android.permission.ACCESS_WIFI_STATE")
    fun checkWifi(enable: Boolean) =
        (enable && (wifiManager?.wifiState == WifiManager.WIFI_STATE_DISABLED || wifiManager?.wifiState == WifiManager.WIFI_STATE_DISABLING)) ||
                (!enable && (wifiManager?.wifiState == WifiManager.WIFI_STATE_ENABLED || wifiManager?.wifiState == WifiManager.WIFI_STATE_ENABLING))

    /**
     * 当一条网络启用到断开会走[ConnectivityManager.NetworkCallback.onAvailable]-[ConnectivityManager.NetworkCallback.onLost]
     * 一条新的网络启用会再走一遍，并且有可能新的网络先回调onAvailable而旧的网络才回调onLost
     * 当网络切换时，如使用流量时启用wifi，而系统默认使用wifi的话，会回调[ConnectivityManager.NetworkCallback.onAvailable]
     *
     * 如果[limitTransportType]则只会回调这些网络的相关状态
     */
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        private var networkId: String? = null

        @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            logHelper.withEnable { "$network onAvailable" }
            networkId = network.toString()
            val networkCapabilities = manager?.getNetworkCapabilities(network)
            arrays.forEach { it.invoke(true, networkCapabilities) }
        }

        /**
         * 信号强度变化也会回调此方法
         */
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            logHelper.withEnable { "$network onCapabilitiesChanged" }
        }

        @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
        override fun onLost(network: Network) {
            super.onLost(network)
            logHelper.withEnable { "$network onLost" }
            //避免可用网络已经回调onAvailable之后上一条网络才回调onLost的状态错误
            if (networkId == network.toString()) {
                val networkCapabilities = manager?.getNetworkCapabilities(network)
                arrays.forEach { it.invoke(false, networkCapabilities) }
            }
        }
    }

    /**
     * 注册网络状态方法有数量限制，如果注册过多而不解除注册会抛出错误
     *
     * @see [ConnectivityManager.registerNetworkCallback]
     */
    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    fun register(): NetHelper {
        manager?.registerNetworkCallback(NetworkRequest.Builder().apply {
            if (this@NetHelper.transportType.isNotEmpty()) {
                this@NetHelper.transportType.forEach {
                    addTransportType(it)
                }
            }
        }.build(), networkCallback)
        return this
    }

    /**
     * 在注册后更改类型需要先解除上一个注册再重新注册
     *@see register
     *
     * @param transportType [NetworkCapabilities.TRANSPORT_WIFI],[NetworkCapabilities.TRANSPORT_CELLULAR]
     */
    fun limitTransportType(vararg transportType: Int): NetHelper {
        this.transportType = transportType
        return this
    }

    fun unregister() {
        manager?.unregisterNetworkCallback(networkCallback)
    }
}