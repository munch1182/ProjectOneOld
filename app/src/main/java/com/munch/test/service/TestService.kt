package com.munch.test.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.munch.lib.log.LogLog

/**
 * Create by munch on 2020/11/30 19:57
 */
class TestService : Service() {

    private val binder by lazy { TestBinder() }

    override fun onCreate() {
        super.onCreate()
        LogLog.log()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogLog.log()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        LogLog.log()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        LogLog.log()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogLog.log()
    }

    private inner class TestBinder : Binder() {
        fun getService() = this@TestService
    }
}