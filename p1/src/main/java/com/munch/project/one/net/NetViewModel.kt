package com.munch.project.one.net

import android.net.NetworkCapabilities
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munch.lib.base.toLive
import com.munch.lib.helper.net.*

/**
 * Create by munch1182 on 2021/9/1 16:55.
 */
class NetViewModel : ViewModel() {

    private val instance = NetHelper.getInstance()
    private val notice = MutableLiveData(getStr())
    fun notice() = notice.toLive()

    init {
        instance.limitTransportType(
            NetworkCapabilities.TRANSPORT_WIFI,
            NetworkCapabilities.TRANSPORT_CELLULAR
        ).add { _, _ ->
            notice.postValue(getStr())
        }
        instance.register()
    }

    private fun getStr(): String {
        val sb = StringBuilder()
        sb.append("正在使用")
        instance.currentNet?.let {
            when {
                it.wifiAvailable() -> sb.append("WIFI")
                it.cellularAvailable() -> sb.append("蜂窝网络")
                else -> {
                }
            }
        }
        instance.allNet
            .mapNotNull { instance.getCapabilities(it) }
            .forEach {
                if (sb.isNotEmpty()) {
                    sb.append("\n\t")
                }
                if (it.hasWifi()) {
                    if (it.wifiAvailable()) {
                        sb.append("WIFI: 可用")
                    } else {
                        sb.append("WIFI: 不可用")
                    }
                }
                if (it.hasCellular()) {
                    if (it.cellularAvailable()) {
                        sb.append("蜂窝网络： 可用")
                    } else {
                        sb.append("蜂窝网络： 不可用")
                    }
                }
            }
        return "网络状态：\t$sb \nIP地址:${instance.getIpAddress()}"
    }

    override fun onCleared() {
        super.onCleared()
        instance.unregister()
    }
}