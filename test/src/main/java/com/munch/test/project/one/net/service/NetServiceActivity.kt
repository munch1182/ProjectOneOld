package com.munch.test.project.one.net.service

import android.annotation.SuppressLint
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import com.munch.pre.lib.extend.ViewHelper
import com.munch.pre.lib.helper.NetStatusHelper
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityNetServiceBinding


/**
 * Create by munch1182 on 2021/4/29 16:04.
 */
class NetServiceActivity : BaseTopActivity() {

    private val helper = AndServiceHelper.INSTANCE
    private val bind by bind<ActivityNetServiceBinding>(R.layout.activity_net_service)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        bind.netServiceStart.setOnClickListener {
            if (helper.isRunning()) {
                bind.netServiceStart.text = "stopping"
                helper.stopWebService()
                NetKeepService.stop(this)
            } else {
                if (NetStatusHelper.wifiAvailable(this)) {
                    bind.netServiceStart.text = "starting"
                    helper.startWebService()
                    NetKeepService.start(this)
                } else {
                    toast("请先打开wifi")
                    return@setOnClickListener
                }
            }
            bind.netServiceStart.isEnabled = false
            it.postDelayed({ showStatus() }, 1000L)
        }
        NetStatusHelper.getInstance(this).apply {
            limitTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            setWhenResume(this@NetServiceActivity,
                { available, _ ->
                    runOnUiThread {
                        if (!available) {
                            bind.netServiceStart.text = "WIFI已关闭"
                            helper.stopWebService()
                            showStatus()
                        }
                    }
                }, { register() }, { unregister() })
        }
    }

    override fun setContentView(view: View) {
        super.setContentView(view, ViewHelper.newMarginParamsMM())
    }

    override fun onResume() {
        super.onResume()
        showStatus()
    }

    private fun showStatus() {
        bind.netServiceStart.text = if (helper.isRunning()) "stop" else "start"
        bind.netServiceStart.isEnabled = true
    }
}