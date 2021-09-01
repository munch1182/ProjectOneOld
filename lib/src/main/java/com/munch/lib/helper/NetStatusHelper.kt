@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import com.munch.lib.app.AppHelper
import com.munch.lib.base.SingletonHolder
import com.munch.lib.log.Logger
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 * Create by munch1182 on 2020/12/28 13:51.
 */
class NetStatusHelper private constructor(context: Context) :
    ARSHelper<(available: Boolean, capabilities: NetworkCapabilities?) -> Unit> {

    private val manager: ConnectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

    companion object : SingletonHolder<NetStatusHelper, Context>({ NetStatusHelper(it) }) {

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

    private fun getNetListenerArray() = arrays

    val currentNet: NetworkCapabilities?
        @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
        get() = manager.getNetworkCapabilities(manager.activeNetwork)

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
            val networkCapabilities = manager.getNetworkCapabilities(network)
            getNetListenerArray().forEach {
                it.invoke(true, networkCapabilities)
            }
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
                val networkCapabilities = manager.getNetworkCapabilities(network)
                getNetListenerArray().forEach {
                    it.invoke(false, networkCapabilities)
                }
            }
        }
    }

    /**
     * 注册网络状态方法有数量限制，如果注册过多而不解除注册会抛出错误
     *
     * @see [ConnectivityManager.registerNetworkCallback]
     */
    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    fun register(): NetStatusHelper {
        manager.registerNetworkCallback(NetworkRequest.Builder().apply {
            if (this@NetStatusHelper.transportType.isNotEmpty()) {
                this@NetStatusHelper.transportType.forEach {
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
    fun limitTransportType(vararg transportType: Int): NetStatusHelper {
        this.transportType = transportType
        return this
    }

    fun unregister() {
        manager.unregisterNetworkCallback(networkCallback)
    }
}