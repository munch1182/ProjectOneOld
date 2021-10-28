package com.munch.lib.notification

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.app.AppHelper
import com.munch.lib.log.Logger

/**
 *
 * 用于监听通知，注意及时关闭监听
 *
 * 需要自行注册实现类
 * <service
 *     android:name="..."
 *     android:exported="true"
 *     android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
 *       <intent-filter>
 *          <action android:name="android.service.notification.NotificationListenerService" />
 *       </intent-filter>
 *  </service>
 *
 * Create by munch1182 on 2021/8/19 10:46.
 */
abstract class NotificationServiceHelper : NotificationListenerService() {

    companion object {

        const val KEY_ENABLE = "KEY_ENABLE"
        const val KEY_SERVICE_CLS = "KEY_SERVICE_CLS"

        @RequiresApi(Build.VERSION_CODES.N)
        fun enable(
            context: Context = AppHelper.app,
            cls: Class<out NotificationServiceHelper> = NotificationServiceHelper::class.java
        ) {
            context.startService(Intent(context, cls).apply {
                putExtra(KEY_ENABLE, true)
                putExtra(KEY_SERVICE_CLS, cls)
            })
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun disable(
            context: Context = AppHelper.app,
            cls: Class<out NotificationServiceHelper> = NotificationServiceHelper::class.java
        ) {
            context.startService(Intent(context, cls).apply {
                putExtra(KEY_ENABLE, false)
                putExtra(KEY_SERVICE_CLS, cls)
            })
        }

        /**
         * 判断是否有读取通知权限
         */
        fun isEnable(context: Context = AppHelper.app) =
            NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)

        fun requestIntent() = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }

    protected open val logNotification = Logger("notification", true)
    private var isConnected = false
        set(value) {
            if (field != value) {
                field = value
                NotificationHelper.onConnectedStateChange(field)
            }
        }
    private var cls: Class<out NotificationServiceHelper>? = null

    @Suppress("unchecked_cast")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return super.onStartCommand(intent, flags, startId)
        cls = intent.getSerializableExtra(KEY_SERVICE_CLS) as Class<out NotificationServiceHelper>?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (intent.getBooleanExtra(KEY_ENABLE, true)) {
                if (!isConnected) {
                    requestRebind(ComponentName(this, getNotificationServerClass()))
                    logNotification.log("requestRebind")
                }
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
        return super.onStartCommand(intent, flags, startId)
    }

    protected open fun getNotificationServerClass(): Class<out NotificationListenerService> =
        cls ?: this::class.java

    /**
     * 当拥有读取通知权限且未调用[requestUnbind]后启动应用会自动回调此方法，但初始化的回调可能会有延迟
     */
    override fun onListenerConnected() {
        super.onListenerConnected()
        logNotification.log("onListenerConnected")
        isConnected = true
    }

    /**
     * 有的手机调用[requestUnbind]也不会回调此方法
     */
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        logNotification.log("onListenerDisconnected")
        isConnected = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return
        logNotification.log("onNotificationPosted")
        NotificationHelper.onPosted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn ?: return
        logNotification.log("onNotificationRemoved")
        NotificationHelper.onRemoved(sbn)
    }
}