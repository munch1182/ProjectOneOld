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
 * Create by munch1182 on 2020/12/15 9:14.
 */
class KeepService : Service() {

    companion object {
        fun start(context: Context) {
            context.startService(Intent(context, KeepService::class.java))
        }
    }

    private val conn: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                log("KeepService onServiceDisconnected:${name}")
                TestDataHelper.keepCount4Guard(this@KeepService)
                startGuardService()
                bindGuardService()
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                log("KeepService onServiceConnected:${name}")
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
        startGuardService()
        bindGuardService()
        TestDataHelper.startGuardTest(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("KeepService onStartCommand")
        return START_STICKY
    }


    private fun bindGuardService() {
        bindService(GuardService.getIntent(), conn, Context.BIND_IMPORTANT)
    }

    private fun startGuardService() {
        startService(GuardService.getIntent())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return object : IGuardConnection.Stub() {
            override fun notifyAlive() {
                log("keepservice IGuardConnection.Stub")
            }
        }
    }
}