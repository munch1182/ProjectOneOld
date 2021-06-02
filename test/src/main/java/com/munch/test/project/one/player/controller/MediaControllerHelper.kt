package com.munch.test.project.one.player.controller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.base.Destroyable
import com.munch.pre.lib.log.Logger

/**
 * Create by munch1182 on 2021/6/1 14:32.
 */
class MediaControllerHelper(
    private val context: Context = BaseApp.getInstance(),
    private val handler: Handler? = BaseApp.getInstance().getThreadHandler()
) : Destroyable {

    companion object {

        fun notificationIntent() = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")

        /**
         * 查询进行中的音乐会话
         *
         * @see NotificationListener
         */
        fun query(
            context: Context = BaseApp.getInstance(),
            manager: MediaSessionManager? = context
                .getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager?
        ): MutableList<MediaController> {
            if (!NotificationListener.isEnabled(context)) {
                return mutableListOf()
            }
            return manager
                ?.getActiveSessions(ComponentName(context, NotificationListener::class.java))
                ?: mutableListOf()
        }

        /**
         * 查询向系统注册过的音乐app
         */
        fun queryApps(pm: PackageManager = BaseApp.getInstance().packageManager): MutableList<ResolveInfo> {
            return pm.queryIntentServices(
                Intent(MediaBrowserServiceCompat.SERVICE_INTERFACE),
                PackageManager.GET_RESOLVED_FILTER
            )
        }
    }

    private var currentController: MediaController? = null
    private var onChange: ((MediaController?) -> Unit)? = null
    private val onActiveSessionsChangedListener by lazy {
        MediaSessionManager.OnActiveSessionsChangedListener {
            currentController = if (!it.isNullOrEmpty()) it[0] else null
            onChange?.invoke(this@MediaControllerHelper.currentController)
        }
    }
    private var manager: MediaSessionManager? = null
    private var callbacks: MutableList<MediaController.Callback?> = mutableListOf()

    /**
     * 获取当前的控制器, 可以直接调用[MediaController]
     *
     * @see setCurrentController
     */
    fun getMediaController(): MediaController? {
        if (currentController == null) {
            if (manager == null) {
                manager =
                    (context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager?)?.apply {
                        addOnActiveSessionsChangedListener(
                            onActiveSessionsChangedListener,
                            ComponentName(context, NotificationListener::class.java), handler
                        )
                    }
            }
            val controllers = query(context, manager)
            if (controllers.isNotEmpty()) {
                currentController = controllers[0]
            }
        }
        return currentController
    }

    fun setCurrentController(currentController: MediaController? = null): MediaControllerHelper {
        this.currentController = currentController
        return this
    }

    fun onControllerChange(onChange: ((MediaController?) -> Unit)? = null): MediaControllerHelper {
        this.onChange = onChange
        return this
    }

    fun volumeUp() {
        getMediaController()
            ?.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    fun volumeDown() {
        getMediaController()
            ?.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }

    fun next() {
        getMediaController()?.transportControls?.skipToNext()
    }

    fun playPause() {
        getMediaController()?.apply {
            if (isPlaying()) {
                transportControls.pause()
            } else {
                transportControls.play()
            }
        }

    }

    fun isPlaying() = getMediaController()?.playbackState?.state == PlaybackState.STATE_PLAYING

    fun previous() {
        getMediaController()?.transportControls?.skipToPrevious()
    }

    fun current(): MediaMetadata? {
        return getMediaController()?.metadata
    }

    fun registerCallback(
        callback: MediaController.Callback,
        handler: Handler? = this.handler
    ): MediaControllerHelper {
        getMediaController()?.registerCallback(callback, handler)
        callbacks.add(callback)
        return this
    }

    fun unregisterCallback(callback: MediaController.Callback) {
        getMediaController()?.unregisterCallback(callback)
        callbacks.remove(callback)
    }

    /**
     * 需要注册
     */
    open class NotificationListener : NotificationListenerService() {

        companion object {

            fun isEnabled(context: Context = BaseApp.getInstance()) =
                NotificationManagerCompat
                    .getEnabledListenerPackages(context)
                    .contains(context.packageName)
        }

        private val log = Logger().apply {
            tag = "notification"
            noStack = true
        }

        override fun onNotificationPosted(sbn: StatusBarNotification?) {
            super.onNotificationPosted(sbn)
            log.log("onNotificationPosted: ${sbn?.packageName}, ${sbn?.notification}")
        }

        override fun onNotificationRemoved(sbn: StatusBarNotification?) {
            super.onNotificationRemoved(sbn)
            log.log("onNotificationRemoved: ${sbn?.packageName}")
        }

    }

    override fun destroy() {
        if (callbacks.isNotEmpty()) {
            callbacks.forEach {
                if (it != null) {
                    getMediaController()?.unregisterCallback(it)
                }
            }
            callbacks.clear()
        }
        onControllerChange()
        if (manager != null) {
            manager?.removeOnActiveSessionsChangedListener(onActiveSessionsChangedListener)
        }
        currentController = null
    }
}