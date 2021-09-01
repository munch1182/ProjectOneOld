package com.munch.project.one.applib.net

import android.net.NetworkCapabilities
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munch.lib.base.toLive
import com.munch.lib.helper.NetStatusHelper

/**
 * Create by munch1182 on 2021/9/1 16:55.
 */
class TestNetViewModel : ViewModel() {

    private val notice = MutableLiveData(getStr())
    fun notice() = notice.toLive()

    init {
        NetStatusHelper.getInstance().limitTransportType(
            NetworkCapabilities.TRANSPORT_WIFI,
            NetworkCapabilities.TRANSPORT_CELLULAR
        ).add { _, _ ->
            notice.postValue(getStr())
        }
        NetStatusHelper.getInstance().register()
    }

    private fun getStr(): String {
        val sb = StringBuilder()
        val it = NetStatusHelper.getInstance().currentNet
        if (it != null) {
            if (it.hasWifi()) {
                if (it.wifiAvailable()) {
                    sb.append("wifi: 可用")
                } else {
                    sb.append("wifi: 不可用")
                }
            } else {
                sb.append("wifi: 无")
            }
            sb.append("\n\t")
            if (it.hasCellular()) {
                if (it.cellularAvailable()) {
                    sb.append("蜂窝网络： 可用")
                } else {
                    sb.append("蜂窝网络： 不可用")
                }
            } else {
                sb.append("蜂窝网络： 无")
            }
        } else {
            sb.append("当前无可使用的网络")
        }
        return "网络状态：\n\t$sb \nIP地址:${NetStatusHelper.getInstance().getIpAddress()}"
    }

    override fun onCleared() {
        super.onCleared()
        NetStatusHelper.getInstance().unregister()
    }
}