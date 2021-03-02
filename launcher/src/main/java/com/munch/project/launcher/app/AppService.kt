package com.munch.project.launcher.app

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.munch.lib.dag.Executor
import com.munch.lib.helper.ServiceBindHelper
import com.munch.project.launcher.app.task.AppItemTask

/**
 * Create by munch1182 on 2021/2/25 15:03.
 */
class AppService : Service() {

    override fun onBind(intent: Intent?): IBinder {
        return ServiceBindHelper.newBinder(this)
    }

    override fun onCreate() {
        super.onCreate()
        Executor.getInstance().add(AppItemTask()).execute()
    }
}