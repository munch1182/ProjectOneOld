package com.munch.project.testsimple.alive.foreground

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.munch.lib.helper.formatDate
import com.munch.lib.helper.startServiceInForeground
import com.munch.lib.log
import com.munch.lib.test.def.DefForegroundService
import com.munch.project.testsimple.R
import com.munch.project.testsimple.alive.TestDataHelper
import kotlin.concurrent.thread

/**
 * Create by munch1182 on 2020/12/14 15:48.
 */
@RequiresApi(Build.VERSION_CODES.M)
class ForegroundService : DefForegroundService() {

    companion object {

        fun start(context: Context) {
            context.startServiceInForeground(Intent(context, ForegroundService::class.java))
        }

        fun startHide(context: Context) {
            context.startServiceInForeground(Intent(context, HideNotificationService::class.java))
        }

    }

    private val binder by lazy { ForegroundServiceBinder() }

    override fun onCreate() {
        super.onCreate()
        log("ForegroundService onCreate")
        //无效
        /*startHide(this)*/
        log("start：" + System.currentTimeMillis())
        TestDataHelper.startForegroundTimerThread(this)
    }

    override fun buildNotification(): Notification {
        return NotificationCompat
            .Builder(this, parameter.channelId)
            .setContentTitle("前台服务运行中")
            .setContentText("${"yyyyMMdd HH:mm:ss".formatDate(System.currentTimeMillis())}开始运行")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(resources,R.mipmap.ic_launcher))
            .build()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("ForegroundService onStartCommand")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
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
    class HideNotificationService : DefForegroundService() {

        override fun onCreate() {
            super.onCreate()
            log("HideNotificationService onCreate")
            thread {
                SystemClock.sleep(1000)
                log("end：" + System.currentTimeMillis())
                stopForeground(true)
                cancel()
                stopSelf()
            }
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