package com.munch.project.one.dev

import android.content.*
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.munch.lib.base.ViewHelper
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.log.log
import com.munch.project.one.dev.databinding.ActivityPlayControlBinding

/**
 * Create by munch1182 on 2021/8/18 10:52.
 */
class PlayControlActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityPlayControlBinding>(R.layout.activity_play_control)
    private val msm by lazy { getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager? }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            log("onReceive")
            refreshMusicInfo()
        }
    }
    private val name by lazy { ComponentName(this, NotificationMonitor::class.java) }
    private val am by lazy { getSystemService(AUDIO_SERVICE) as? AudioManager? }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            playOrPause.setImageDrawable(
                ViewHelper.newSelectDrawable(
                    getDrawableCompat(R.drawable.ic_play_arrow),
                    getDrawableCompat(R.drawable.ic_pause)
                )
            )
            next.setOnClickListener { playNext() }
            previous.setOnClickListener { playPrevious() }
            playOrPause.setOnClickListener { playOrPause() }
        }
        checkPermission2SHowView()
    }

    private fun getDrawableCompat(drawableRes: Int): Drawable {
        return AppCompatResources.getDrawable(this@PlayControlActivity, drawableRes)!!
    }

    private fun playOrPause() {
        if (am != null) {
            if (am!!.isMusicActive) {
                controlMusic(KeyEvent.KEYCODE_MEDIA_PAUSE)
            } else {
                controlMusic(KeyEvent.KEYCODE_MEDIA_PLAY)

            }
        }
    }

    private fun playPrevious() {
        controlMusic(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    private fun playNext() {
        controlMusic(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    private fun controlMusic(keyCode: Int) {
        val eventTime = SystemClock.uptimeMillis()
        val key = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
        dispatchMediaKeyToAudioService(key)
        dispatchMediaKeyToAudioService(KeyEvent.changeAction(key, KeyEvent.ACTION_UP))
    }

    private fun dispatchMediaKeyToAudioService(event: KeyEvent) {
        try {
            am?.dispatchMediaKeyEvent(event)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPermission2SHowView() {
        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)) {
            noPermissionView()
        } else {
            noPlayerView()
            enableNotify()
            initReceiver()
            refreshMusicInfo()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkPermission2SHowView()
    }

    private fun initReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
            IntentFilter().apply {
                addAction(ACTION_NOTIFICATION_POSTED)
                addAction(ACTION_NOTIFICATION_REMOVED)
            })

        try {
            msm?.addOnActiveSessionsChangedListener({
                log("onActiveSessionsChanged")
                refreshMusicInfo()
            }, name)
            val callback = object : MediaController.Callback() {
                override fun onSessionEvent(event: String, extras: Bundle?) {
                    super.onSessionEvent(event, extras)
                    log("onSessionEvent")
                    refreshMusicInfo()
                }

                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    super.onPlaybackStateChanged(state)
                    log("onPlaybackStateChanged")
                    refreshMusicInfo()
                }

                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    super.onMetadataChanged(metadata)
                    log("onMetadataChanged")
                    metadata ?: return
                    refreshMusicInfo()
                }
            }
            msm?.getActiveSessions(name)?.forEach { it.registerCallback(callback) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val iF = IntentFilter()
        iF.addAction("com.android.music.metachanged")
        iF.addAction("com.android.music.playstatechanged")
        iF.addAction("com.android.music.playbackcomplete")
        iF.addAction("com.android.music.queuechanged")

        iF.addAction("com.miui.player.metachanged")
        iF.addAction("com.miui.player.playstatechanged")
        iF.addAction("com.oppo.music.service.meta_changed")
        iF.addAction("com.oppo.music.service.playstate_changed")
        iF.addAction("com.kugou.android.music.metachanged")
        iF.addAction("com.ting.mp3.playinfo_changed")

        iF.addAction("fm.last.android.metachanged")
        iF.addAction("fm.last.android.playbackpaused")
        iF.addAction("com.sec.android.app.music.metachanged")
        iF.addAction("com.nullsoft.winamp.metachanged")
        iF.addAction("com.nullsoft.winamp.playstatechanged")
        iF.addAction("com.amazon.mp3.metachanged")
        iF.addAction("com.amazon.mp3.playstatechanged")
        iF.addAction("com.real.IMP.metachanged")
        iF.addAction("com.real.IMP.playstatechanged")
        iF.addAction("com.sonyericsson.music.metachanged")
        iF.addAction("com.sonyericsson.music.playstatechanged")
        iF.addAction("com.rdio.android.metachanged")
        iF.addAction("com.rdio.android.playstatechanged")
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged")
        iF.addAction("com.samsung.sec.android.MusicPlayer.playstatechanged")
        iF.addAction("com.andrew.apollo.metachanged")
        iF.addAction("com.andrew.apollo.playstatechanged")
        iF.addAction("com.htc.music.metachanged")
        iF.addAction("com.htc.music.playstatechanged")
        iF.addAction("com.spotify.music.playbackstatechanged")
        iF.addAction("com.spotify.music.metadatachanged")
        iF.addAction("com.rhapsody.playstatechanged")
        registerReceiver(receiver, iF)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun refreshMusicInfo() {
        val controller = msm?.getActiveSessions(name)?.takeIf { it.isNotEmpty() }?.get(0) ?: return

        /*log("refreshMusicInfo")*/
        val pm = packageManager
        val playerName = pm.getApplicationInfo(controller.packageName, PackageManager.GET_META_DATA)
            .loadLabel(pm).toString()

        val pbState = controller.playbackState ?: return
        val isPlaying = pbState.state == PlaybackState.STATE_PLAYING
        val speed = pbState.playbackSpeed

        val meta = controller.metadata ?: return
        val duration = meta.getLong(MediaMetadata.METADATA_KEY_DURATION)
        var current =
            (SystemClock.elapsedRealtime() - pbState.lastPositionUpdateTime) * speed + pbState.position
        if (current > duration) {
            current = 0f
        }

        val artist = meta.getText(MediaMetadata.METADATA_KEY_ARTIST)
        val title = meta.description.title

        val infoStr = "isPlaying:$isPlaying,\n$current/$duration/$speed,\n$title,\n$artist"
        havePlayerView(isPlaying, playerName, infoStr)
    }

    private fun noPermissionView() {
        bind.apply {
            info.text = "当前无获取通知权限，点击获取"
            info.setOnClickListener { requestNotify() }
            groupData.visibility = View.GONE
        }
    }

    private fun enableNotify() {
        val pm = packageManager
        pm.setComponentEnabledSetting(
            ComponentName(this, NotificationMonitor::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )

        pm.setComponentEnabledSetting(
            ComponentName(this, NotificationMonitor::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }

    private fun requestNotify() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }

    private fun noPlayerView() {
        bind.apply {
            info.text = "当前无音乐播放"
            groupData.visibility = View.GONE
        }
    }

    private fun havePlayerView(isPlaying: Boolean, playerName: String, info: String) {
        bind.apply {
            playOrPause.isSelected = isPlaying
            groupData.visibility = View.VISIBLE
            bind.meta.text = info
            bind.info.text = playerName
        }
    }

    class NotificationMonitor : NotificationListenerService() {

        override fun onNotificationPosted(sbn: StatusBarNotification?) {
            super.onNotificationPosted(sbn)
            /*log("onNotificationPosted")*/
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(ACTION_NOTIFICATION_POSTED))
        }

        override fun onNotificationRemoved(sbn: StatusBarNotification?) {
            super.onNotificationRemoved(sbn)
            /*log("onNotificationRemoved")*/
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(ACTION_NOTIFICATION_REMOVED))
        }
    }

    companion object {
        const val ACTION_NOTIFICATION_POSTED = "com.munch.project.one.dev.NOTIFICATION_POSTED"
        const val ACTION_NOTIFICATION_REMOVED = "com.munch.project.one.dev.NOTIFICATION_REMOVED"
    }
}