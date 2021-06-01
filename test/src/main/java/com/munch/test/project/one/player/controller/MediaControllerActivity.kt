package com.munch.test.project.one.player.controller

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaMetadata
import android.media.session.MediaController
import android.os.Bundle
import android.widget.Button
import com.munch.pre.lib.extend.clickItem
import com.munch.pre.lib.extend.getAttrArrayFromTheme
import com.munch.pre.lib.extend.getColorCompat
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityMediaControllerBinding

/**
 * Create by munch1182 on 2021/6/1 11:17.
 */
@SuppressLint("SetTextI18n")
class MediaControllerActivity : BaseTopActivity() {

    private val bind by bind<ActivityMediaControllerBinding>(R.layout.activity_media_controller)
    private val colorPrimary by lazy {
        getAttrArrayFromTheme(com.munch.pre.lib.R.attr.colorPrimary) {
            getColor(0, Color.RED)
        }
    }
    private val colorGray by lazy { getColorCompat(R.color.colorTextGray) }
    private val callback: MediaController.Callback by lazy {
        object : MediaController.Callback() {

            override fun onMetadataChanged(metadata: MediaMetadata?) {
                super.onMetadataChanged(metadata)
                showInfo(metadata)
            }
        }
    }
    private val helper by lazy {
        MediaControllerHelper().onControllerChange { showPlayer(it) }.registerCallback(callback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            if (!MediaControllerHelper.NotificationListener.isEnabled()) {
                controllerInfo.text = "need permission"
            } else {
                showPlayer(helper.getMediaController())
            }
            controllerContainer.clickItem({ _, i ->
                when (i) {
                    0 -> previous()
                    1 -> next()
                    2 -> playPause()
                    3 -> volumeDown()
                    4 -> volumeUp()
                    5 -> permission()
                }
            }, Button::class.java)
        }
    }

    override fun onResume() {
        super.onResume()
        bind.controllerPermission.setBackgroundColor(if (MediaControllerHelper.NotificationListener.isEnabled()) colorGray else colorPrimary)
    }

    override fun onDestroy() {
        super.onDestroy()
        helper.onControllerChange()
        helper.unregisterCallback(callback)
    }

    private fun permission() {
        startActivity(MediaControllerHelper.notificationIntent())
    }

    private fun volumeUp() {
        showOp("volumeUp")
        helper.volumeUp()
    }

    private fun volumeDown() {
        showOp("volumeDown")
        helper.volumeDown()
    }

    private fun next() {
        showOp("next")
        helper.next()
    }

    private fun playPause() {
        showOp("playPause")
        helper.playPause()
    }

    private fun previous() {
        showOp("previous")
        helper.previous()
    }

    private fun showPlayer(it: MediaController?) {
        bind.controllerPlayer.apply {
            runOnUiThread {
                text = it?.packageName ?: "no active sessions"
            }
        }
    }

    private fun showOp(op: String) {
        bind.controllerOp.text = "operate:$op"
    }

    private fun showInfo(info: MediaMetadata?) {
        bind.controllerInfo.apply {
            runOnUiThread {
                text = if (info == null) "" else String.format(
                    "%s\n%s",
                    info.getString(MediaMetadata.METADATA_KEY_TITLE),
                    info.getString(MediaMetadata.METADATA_KEY_ARTIST)
                )
            }
        }
    }
}