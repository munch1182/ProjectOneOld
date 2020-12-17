package com.munch.project.testsimple.alive.foreground

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.annotation.RequiresApi
import com.munch.lib.log
import com.munch.project.testsimple.R
import com.munch.project.testsimple.alive.TestDataHelper

/**
 * Create by munch1182 on 2020/12/14 15:48.
 */
@RequiresApi(Build.VERSION_CODES.O)
class ForegroundService : Service() {

    companion object {
        const val ID_FOREGROUND_SERVICE = 110
        private const val CHANNEL_ONE_ID = "BLE SERVICE"
        private const val CHANNEL_ONE_NAME = "BLE service"

        @RequiresApi(Build.VERSION_CODES.O)
        fun start(context: Context) {
            context.startForegroundService(Intent(context, ForegroundService::class.java))
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun startHide(context: Context) {
            context.startForegroundService(Intent(context, HideNotificationService::class.java))
        }

        fun createNotification(context: Context, manager: NotificationManager): Notification {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            return Notification.Builder(
                context,
                CHANNEL_ONE_ID
            )
                .setContentTitle("Test Alive")
                .setContentText("foreground service")
                .setSmallIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .build()
        }
    }

    private val binder by lazy { ForegroundServiceBinder() }
    private val manager by lazy { getSystemService(NotificationManager::class.java) as NotificationManager }

    override fun onCreate() {
        super.onCreate()
        log("ForegroundService onCreate")
        //无效
        /*startHide(this)*/
        log("start：" + System.currentTimeMillis())
        startForeground(ID_FOREGROUND_SERVICE, createNotification(this, manager))

        TestDataHelper.startForegroundTimerThread(this)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("ForegroundService onStartCommand")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        log("ForegroundService onBind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        log("ForegroundService onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        log("ForegroundService onDestroy")
    }

    inner class ForegroundServiceBinder : Binder()

    /**
     * 使用相同id创建的前台服务，关闭时会取消图标显示
     */
    class HideNotificationService : Service() {

        override fun onCreate() {
            super.onCreate()
            log("HideNotificationService onCreate")
            Thread(Runnable {
                val manager =
                    getSystemService(NotificationManager::class.java) as NotificationManager
                startForeground(
                    ID_FOREGROUND_SERVICE,
                    createNotification(
                        this,
                        manager
                    )
                )
                SystemClock.sleep(1000)
                log("end：" + System.currentTimeMillis())
                stopForeground(true)
                manager.cancel(ID_FOREGROUND_SERVICE)
                stopSelf()
            }).start()
        }

        override fun onDestroy() {
            super.onDestroy()
            log("HideNotificationService onDestroy")
        }

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }
    }
}