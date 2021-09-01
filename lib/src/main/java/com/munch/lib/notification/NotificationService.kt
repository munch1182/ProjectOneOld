package com.munch.lib.notification

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.app.AppHelper
import com.munch.lib.base.OnChangeListener
import com.munch.lib.log.Logger

/**
 *
 * 用于监听通知
 *
 * Create by munch1182 on 2021/8/19 10:46.
 */
open class NotificationService : NotificationListenerService() {

    companion object {

        private const val KEY_ENABLE = "KEY_ENABLE"

        @RequiresApi(Build.VERSION_CODES.N)
        fun enable(context: Context = AppHelper.app) {
            context.startService(Intent(context, NotificationService::class.java).apply {
                putExtra(KEY_ENABLE, true)
            })
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun disable(context: Context = AppHelper.app) {
            context.startService(Intent(context, NotificationService::class.java).apply {
                putExtra(KEY_ENABLE, false)
            })
        }

        private var changeListener: (() -> Unit)? = null

        /**
         * 判断是否有读取通知权限
         */
        fun isEnable(context: Context = AppHelper.app) =
            NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)

        fun requestIntent() = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")

        /**
         * @see unregisterOnNotificationChangeListener
         */
        fun registerOnNotificationChangeListener(onChange: () -> Unit) {
            this.changeListener = onChange
        }

        /**
         * @see registerOnNotificationChangeListener
         */
        fun unregisterOnNotificationChangeListener() {
            this.changeListener = null
        }

        fun getNotificationChangeListener() = changeListener

    }

    interface OnNotificationChangeListener : OnChangeListener

    protected open val logNotification = Logger().apply {
        tag = "notification"
        noStack = true
    }
    private var isConnected = false
        set(value) {
            if (field != value) {
                field = value
            }
        }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (intent.getBooleanExtra(KEY_ENABLE, true)) {
                    requestRebind(ComponentName(this, getNotificationServerClass()))
                    logNotification.log("requestRebind")
                } else {
                    try {
                        requestUnbind()
                        logNotification.log("requestUnbind")
                        isConnected = false
                    } catch (e: Exception) {
                        //ignore
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    protected open fun getNotificationServerClass(): Class<out NotificationListenerService> =
        this::class.java

    /**
     * 当拥有读取通知权限且未调用[requestUnbind]后启动应用会自动回调此方法，但初始化的回调可能会有延迟
     */
    override fun onListenerConnected() {
        super.onListenerConnected()
        isConnected = true
        logNotification.log("onListenerConnected")
    }


    /**
     * 有的手机调用[requestUnbind]也不会回调此方法
     */
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
        logNotification.log("onListenerDisconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        getNotificationChangeListener()?.invoke()
        logNotification.log("onNotificationPosted")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        getNotificationChangeListener()?.invoke()
        logNotification.log("onNotificationRemoved")
    }

}