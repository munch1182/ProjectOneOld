package com.munch.lib.android.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.provider.Settings
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentActivity
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.*
import com.munch.lib.android.result.ResultHelper

/**
 * 主要由三部分组成:
 * 1. 由诸如[notify]等方法组成的公共方法
 * 2. [withService]用于设置绑定服务的通知及其更新, 一个服务对应一个对象
 * 3. 由[ARSHelper]和[NotificationListenerService]组成的监听通知的对应方法
 * Create by munch1182 on 2022/10/20 16:25.
 */
object NotificationHelper : ARSHelper<NotificationHelper.OnNotificationListener>() {

    val nm by lazy {
        AppHelper.getSystemService(Context.NOTIFICATION_SERVICE).to<NotificationManager>()
    }

    /**
     * 检查发出通知权限
     */
    fun checkIsEnableNotification(activity: FragmentActivity) =
        ResultHelper.with(activity)
            .judge { NotificationManagerCompat.from(it).areNotificationsEnabled() }
            .intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)

    /**
     * 删除本应用的所有的channel
     */
    fun delOtherChannel(
        nm: NotificationManager = AppHelper.getSystemService(Context.NOTIFICATION_SERVICE).to()
    ) {
        nm.notificationChannels.forEach { nm.deleteNotificationChannel(it.id) }
    }

    /**
     * 创建一个通知渠道并返回
     */
    fun createChannel(name: CharSequence, id: String = name.toString()) =
        NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            .apply { nm.createNotificationChannel(this) }

    /**
     * 立刻发出一条通知
     *
     * @param channelId 通知渠道, 该名字会显示到通知的设置中
     * @param id 该消息的id, 不同的id显示为多条消息, 相同的id会更新, 使用中注意不要与其它固定id的通知冲突否则会将其覆盖
     */
    fun notify(
        channelId: String,
        id: Int = 11111,
        update: NotificationCompat.Builder.() -> Unit
    ) {
        val notification = nm.activeNotifications
            ?.find { it.notification.channelId == channelId }
            ?.notification
        if (notification == null) {
            createChannel(channelId)
            nm.notify(id, NotificationCompat.Builder(AppHelper, channelId).apply(update).build())
        } else {
            nm.notify(id, NotificationCompat.Builder(AppHelper, notification).apply(update).build())
        }
    }

    /**
     * 删除该channel及其下的所有已发出的消息
     */
    fun del(channelId: String) {
        nm.deleteNotificationChannel(channelId)
    }

    //<editor-fold desc="绑定服务的通知">
    /**
     * 通过[Context.bindService]的方法创建前台服务
     *
     * 需要自建返回一个[Service.onBind]返回[IServiceBinder]的Service:
     * class NotifyService : Service() {
     *      override fun onBind(p0: Intent?): IBinder {
     *          return ServiceBinder()
     *      }
     *      inner class ServiceBinder : Binder(), NotificationHelper.IServiceBinder {
     *          override fun getService(): Service {
     *              return this@NotifyService
     *          }
     *      }
     * }
     * 且该service需要注册
     */
    fun withService(
        channelName: String,
        serviceId: Int,
        serviceType: Int = ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
    ): NotificationBindServiceHelper {
        return NotificationBindServiceHelper(channelName, serviceId, serviceType)
    }

    interface IServiceBinder {

        fun getService(): Service
    }

    class NotificationBindServiceHelper(
        private val name: String,
        private val serviceId: Int,
        private val serviceType: Int = ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE,
    ) {
        private val channel by lazy { createChannel(name) }

        val conn = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                if (p1 is IServiceBinder) {
                    service = p1.getService()
                    onBind()
                }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                service = null
            }

        }

        private var service: Service? = null

        fun isBind() = getInMain { service != null }

        /**
         * 注意: 有的手机系统会自动因此无内容的notification, 因此在创建之后应该即时[notify]更新内容
         */
        @RequiresPermission(android.Manifest.permission.FOREGROUND_SERVICE)
        inline fun <reified SERVICE : Service> start() {
            impInMain {
                if (isBind()) return@impInMain
                AppHelper.bindService(
                    Intent(AppHelper, SERVICE::class.java),
                    conn,
                    Context.BIND_AUTO_CREATE
                )
            }
        }

        fun stop() {
            impInMain {
                if (!isBind()) return@impInMain
                service = null
                conn.let { AppHelper.unbindService(it) }
            }
        }

        /**
         * 此处执行该操作, 则不需要service中再执行
         */
        private fun onBind() {
            service?.startForeground(
                serviceId,
                NotificationCompat.Builder(AppHelper, channel.id).build(),
                serviceType
            )
        }

        fun notify(update: NotificationCompat.Builder.() -> Unit) {
            notify(channel.id, serviceId, update)
        }


    }
    //</editor-fold>

    //<editor-fold desc="监听通知栏">
    /**
     * 检查监听通知栏权限
     */
    fun checkIsEnableListenNotification(activity: FragmentActivity) =
        ResultHelper.with(activity)
            .judge {
                NotificationManagerCompat.getEnabledListenerPackages(AppHelper)
                    .contains(AppHelper.packageName)
            }
            .intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

    /**
     * 用于监听和回调[NotificationListenerService]的变更并传递给[NotificationHelper]
     */
    internal val notificationListener = object : OnNotificationListener {
        override fun onPost(sbn: StatusBarNotification?) {
            update { it.onPost(sbn) }
        }

        override fun onRemoved(sbn: StatusBarNotification?) {
            update { it.onRemoved(sbn) }
        }

    }

    interface OnNotificationListener {
        fun onPost(sbn: StatusBarNotification?)
        fun onRemoved(sbn: StatusBarNotification?)
    }

    /**
     * 需要注册
     *
     * <service
     *      android:name="com.munch.lib.android.helper.NotificationHelper$NotificationListenerService"
     *      android:exported="true"
     *      android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
     *      <intent-filter>
     *          <action android:name="android.service.notification.NotificationListenerService" />
     *      </intent-filter>
     * </service>
     */
    class NotificationListenerService : android.service.notification.NotificationListenerService() {

        override fun onNotificationPosted(sbn: StatusBarNotification?) {
            super.onNotificationPosted(sbn)
            notificationListener.onPost(sbn)
        }

        override fun onNotificationRemoved(sbn: StatusBarNotification?) {
            super.onNotificationRemoved(sbn)
            notificationListener.onRemoved(sbn)
        }
    }
    //</editor-fold>
}