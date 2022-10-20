package com.munch.project.one.notify

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.core.graphics.drawable.IconCompat
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.fmt
import com.munch.lib.android.helper.NotificationHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.fastview.fvLLBtn
import com.munch.lib.fast.view.newRandomString
import com.munch.project.one.R
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

/**
 * Create by munch1182 on 2022/10/20 16:35.
 */
class NotifyActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private val bind by fvLLBtn("start", "stop", "update")
    private val notify = NotificationHelper("test notification", 121)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.clickByStr {
            when (it) {
                "start" -> notify.start<NotifyTestService>()
                "stop" -> notify.stop()
                "update" -> {
                    notify.notify {
                        setSmallIcon(IconCompat.createWithResource(AppHelper, R.mipmap.ic_launcher))
                        setContentTitle(newRandomString())
                        setContentText(System.currentTimeMillis().fmt())
                    }
                }
            }
        }
    }

    class NotifyTestService : Service() {

        override fun onBind(p0: Intent?): IBinder {
            return ServiceBinder()
        }

        inner class ServiceBinder : Binder(), NotificationHelper.IServiceBinder {
            override fun getService(): Service {
                return this@NotifyTestService
            }

        }
    }
}