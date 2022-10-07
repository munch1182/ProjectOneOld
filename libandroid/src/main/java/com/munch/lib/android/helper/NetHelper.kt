package com.munch.lib.android.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.catch
import com.munch.lib.android.extend.lazy
import com.munch.lib.android.log.Logger
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Create by munch1182 on 2022/10/7 15:59.
 */
object NetHelper : LiveData<Network?>() {

    private val log = Logger.only("net")

    private val cm by lazy {
        AppHelper.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    /**
     * 当前使用的network
     */
    val curr: Network?
        get() = cm.activeNetwork

    private val netCapabilities: Array<Int> =
        arrayOf(
            NetworkCapabilities.TRANSPORT_WIFI,
            NetworkCapabilities.TRANSPORT_CELLULAR,
            NetworkCapabilities.TRANSPORT_ETHERNET
        )

    private val defRequest: NetworkRequest = NetworkRequest.Builder()
        .apply { netCapabilities.forEach { addTransportType(it) } }
        .build()

    private var setRequest: NetworkRequest? = null
    private val request: NetworkRequest
        get() = setRequest ?: defRequest
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            log.log("onAvailable ${getName(network)}($network)")
            postNow()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            log.log("onLost ${getName(network)}($network)")
            postNow()
        }
    }

    private fun postNow() {
        postValue(curr)
    }

    override fun onActive() {
        super.onActive()
        register()
        postNow()
    }

    override fun onInactive() {
        super.onInactive()
        unregister()
    }

    fun setNetRequest(request: NetworkRequest): NetHelper {
        this.setRequest = request
        return this
    }

    fun register(request: NetworkRequest = this.request) {
        catch {
            cm.registerNetworkCallback(request, callback)
            log.log("register network callback.")
        }
    }

    fun unregister() {
        catch {
            cm.unregisterNetworkCallback(callback)
            log.log("unregister network callback.")
        }
    }

    /**
     * 判断network是否能够连接网络
     *
     * @see netCapabilities
     */
    fun isConnected(network: Network? = curr): Boolean {
        network ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return false
        }
        return netCapabilities.any { capabilities.hasTransport(it) }
    }

    /**
     * 获取netWork的名称(自定义)
     */
    fun getName(network: Network? = curr): String? {
        network ?: return null
        val capabilities = cm.getNetworkCapabilities(network) ?: return null
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "BLUETOOTH"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB) -> "USB"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) -> "LOWPAN"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> "WIFI AWARE"
            else -> null
        }
    }

    /**
     * 获取network的联网能力
     */
    fun getCapabilities(network: Network? = curr): Int? {
        network ?: return null
        val capabilities = cm.getNetworkCapabilities(network) ?: return null
        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return NetworkCapabilities.TRANSPORT_WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return NetworkCapabilities.TRANSPORT_CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return NetworkCapabilities.TRANSPORT_ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> return NetworkCapabilities.TRANSPORT_BLUETOOTH
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> return NetworkCapabilities.TRANSPORT_VPN
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return NetworkCapabilities.TRANSPORT_WIFI_AWARE
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return NetworkCapabilities.TRANSPORT_USB
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) return NetworkCapabilities.TRANSPORT_LOWPAN
        }
        return null
    }

    fun getLinkProperties(network: Network?) = cm.getLinkProperties(network)
}

/**
 * 获取网络地址
 */
fun getNetAddress() = catch<String?> {
    for (enumeration in NetworkInterface.getNetworkInterfaces()) {
        for (address in enumeration.inetAddresses) {
            if (!address.isLoopbackAddress && address is Inet4Address) {
                return address.hostAddress
            }
        }
    }
    null
}

@Suppress("NOTHING_TO_INLINE")
inline fun Network.getName() = NetHelper.getName(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Network.isConnected() = NetHelper.isConnected(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Network.getLinkProperties() = NetHelper.getLinkProperties(this)