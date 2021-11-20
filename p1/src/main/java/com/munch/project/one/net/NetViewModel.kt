package com.munch.project.one.net

import android.net.NetworkCapabilities
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munch.lib.base.toImmutable
import com.munch.lib.helper.net.NetHelper
import com.munch.lib.helper.net.cellularAvailable
import com.munch.lib.helper.net.wifiAvailable

/**
 * Create by munch1182 on 2021/9/1 16:55.
 */
class NetViewModel : ViewModel() {

    private val instance = NetHelper.getInstance()
    private val notice = MutableLiveData(getStr())
    fun notice() = notice.toImmutable()

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
        return "网络状态：\t$sb \nIP地址:${instance.getIpAddress()}"
    }

    override fun onCleared() {
        super.onCleared()
        instance.unregister()
    }
}