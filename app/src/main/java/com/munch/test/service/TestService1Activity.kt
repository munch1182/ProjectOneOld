package com.munch.test.service

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import com.munch.lib.libnative.helper.ViewHelper
import com.munch.lib.log.LogLog
import com.munch.test.R
import com.munch.test.base.BaseActivity
import kotlinx.android.synthetic.main.activity_test_service.*

/**
 * Create by Munch on 2020/09/04
 */
class TestService1Activity : BaseActivity() {

    companion object {
        const val EXTRA_NAME = "EXTRA_NAME"
    }

    private val serviceIntent by lazy { Intent(this, TestService::class.java) }
    private var serviceBind = false
    private val conn = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            serviceBind = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBind = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_service)

        var index = intent.getIntExtra(EXTRA_NAME, 0)
        setToolBar(view_tb, "View$index")

        ViewHelper.clickItem(
            service_container,
            View.OnClickListener {
                if (it.tag is Int) {
                    when (it.tag) {
                        0 -> {
                            startService(serviceIntent)
                        }
                        1 -> {
                            stopService(serviceIntent)
                        }
                        2 -> {
                            LogLog.log(ServiceHelper.getService(this))
                        }
                        3 -> {
                            bindService(serviceIntent, conn, BIND_AUTO_CREATE)
                        }
                        4 -> {
                            unbindService(conn)
                        }
                        5 -> {
                            startActivity(Intent(this, TestService1Activity::class.java).apply {
                                putExtra(EXTRA_NAME, ++index)
                            })
                        }
                    }
                }
            }, Button::class.java
        )
    }

    fun notice(notice: String) {
        runOnUiThread { service_tv_notice.text = notice }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(serviceIntent)
    }

}