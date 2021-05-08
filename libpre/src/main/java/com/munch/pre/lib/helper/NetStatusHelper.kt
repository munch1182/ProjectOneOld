@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.pre.lib.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresPermission
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.extend.SingletonHolder
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 * Create by munch1182 on 2020/12/28 13:51.
 */
class NetStatusHelper private constructor(private val manager: ConnectivityManager) :
    ARSHelper<(available: Boolean, capabilities: NetworkCapabilities?) -> Unit>() {

    companion object : SingletonHolder<NetStatusHelper, ConnectivityManager>(::NetStatusHelper) {

        @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
        fun getInstance(context: Context = BaseApp.getInstance()): NetStatusHelper {
            return getInstance((context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager))
        }

        @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
        fun wifiAvailable(context: Context = BaseApp.getInstance()): Boolean {
            val manager =
                context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                    ?: return false
            val capabilities =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.getNetworkCapabilities(manager.activeNetwork)
                } else {
                    val allNetworks = manager.allNetworks
                    if (allNetworks.isEmpty()) {
                        null
                    } else {
                        manager.getNetworkCapabilities(allNetworks[allNetworks.lastIndex])
                    }
                } ?: return false
            return wifiAvailable(capabilities)
        }

        fun wifiAvailable(networkCapabilities: NetworkCapabilities): Boolean {
            var available = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            available =
                    //wifi网络功能开启
                available && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                available =
                        //wifi网络已验证连接
                    available && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
            return available
        }

        //流量可用
        fun cellularAvailable(networkCapabilities: NetworkCapabilities): Boolean {
            var available = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            available =
                available && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                available =
                    available && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
            return available
        }

        fun vpnAvailable(networkCapabilities: NetworkCapabilities): Boolean {
            var available = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            available =
                available && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                available =
                    available && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
            return available
        }

        /**
         * 获取网络状态下的ip，包括wifi和4g网
         * 且无需额外权限
         */
        @RequiresPermission("android.permission.INTERNET")
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

        @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
        fun getActiveNet(manager: ConnectivityManager? = getManager()): Network? {
            manager ?: return null
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.activeNetwork
            } else {
                val allNetworks = manager.allNetworks
                if (allNetworks.isEmpty()) {
                    null
                } else {
                    allNetworks[allNetworks.lastIndex]
                }
            }
        }

        @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
        fun getLink(manager: ConnectivityManager? = getManager()) = manager?.getLinkProperties(
            getActiveNet(manager)
        )

        @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
        fun getCapabilities(manager: ConnectivityManager? = getManager()) =
            manager?.getNetworkCapabilities(getActiveNet(manager))

        private fun getManager() =
            BaseApp.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    }

    private var capabilities: NetworkCapabilities? = null
    private var transportType = intArrayOf()

    private fun getNetListenerArray() = arrays

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
            networkId = network.toString()
            capabilities =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.getNetworkCapabilities(manager.activeNetwork)
                } else {
                    //当一条网络可用时启用另一条可用网络，则allNetworks有多个参数
                    val allNetworks = manager.allNetworks
                    if (allNetworks.isEmpty()) {
                        null
                    } else {
                        manager.getNetworkCapabilities(allNetworks[allNetworks.lastIndex])
                    }
                }
            getNetListenerArray().forEach {
                it.invoke(true, capabilities)
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
            capabilities = networkCapabilities
        }


        override fun onLost(network: Network) {
            super.onLost(network)
            //避免可用网络已经回调onAvailable之后上一条网络才回调onLost的状态错误
            if (networkId == network.toString()) {
                getNetListenerArray().forEach {
                    it.invoke(false, null)
                }
            }
        }
    }

    /**
     * @see [wifiAvailable],[cellularAvailable]
     */
    fun getCurrentNetCapabilities() = capabilities

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