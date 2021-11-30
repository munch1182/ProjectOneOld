package com.munch.lib.helper.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.app.AppHelper
import com.munch.lib.base.startServiceInForeground

/**
 * Create by munch1182 on 2021/4/25 16:03.
 */
open class BaseForegroundService(override val parameter: IForegroundService.Parameter) : Service(),
    IForegroundService {

    companion object {

        @RequiresPermission("android.permission.FOREGROUND_SERVICE")
        fun startForegroundService(context: Context, intent: Intent) {
            context.startServiceInForeground(intent)
        }

        fun stop(context: Context, intent: Intent) {
            context.stopService(intent)
        }

        @RequiresPermission("android.permission.FOREGROUND_SERVICE")
        fun start() {
            startForegroundService(
                AppHelper.app,
                Intent(AppHelper.app, BaseForegroundService::class.java)
            )
        }

        fun stop() {
            stop(AppHelper.app, Intent(AppHelper.app, BaseForegroundService::class.java))
        }

        const val DEF_SERVICE_ID = 1130
        const val DEF_CHANNEL_ID = "ForegroundService"
    }

    constructor() : this(
        IForegroundService.Parameter(
            DEF_CHANNEL_ID,
            DEF_CHANNEL_ID,
            DEF_SERVICE_ID
        )
    )

    override val service: Service by lazy { this }
    override val manager by lazy { NotificationManagerCompat.from(this) }

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}