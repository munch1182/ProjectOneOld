package com.munch.project.testsimple.alive.guard

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import com.munch.lib.log
import com.munch.project.testsimple.IGuardConnection
import com.munch.project.testsimple.alive.TestDataHelper

/**
 * 此service的进程不在主进程，查看log时需注意，不能过滤
 * Create by munch1182 on 2020/12/15 9:13.
 */
class GuardService : Service() {

    companion object {

        fun getIntent(pkgName: String = "com.munch.project.testsimple"): Intent {
            return Intent().setAction("com.munch.project.guard").setPackage(pkgName)
        }
    }

    private val conn: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                log("GuardService onServiceDisconnected:${name}")
                restartKeepService()
                bindKeepService()
                TestDataHelper.guardCount4Keep(this@GuardService)
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                log("GuardService onServiceConnected:${name}")
                try {
                    IGuardConnection.Stub.asInterface(service).notifyAlive()
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        log("guard service onCreate")
        restartKeepService()
        bindKeepService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("guard service onStartCommand")
        return START_STICKY
    }


    private fun bindKeepService() {
        bindService(Intent(this, KeepService::class.java), conn, Context.BIND_IMPORTANT)
    }

    private fun restartKeepService() {
        startService(Intent(this, KeepService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        log("guardservice onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return object : IGuardConnection.Stub() {
            override fun notifyAlive() {
                log("guardservice IGuardConnection.Stub")
            }
        }
    }
}