package com.munch.test.project.one.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.contains
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.munch.test.project.one.R
import com.munch.test.project.one.databinding.LayoutVideoControllerBinding
import com.munch.test.project.one.player.media.*
import com.munch.test.project.one.player.video.VideoView
import java.lang.UnsupportedOperationException

/**
 * Create by munch1182 on 2021/5/11 14:41.
 */
class DefMediaControllerView : MediaControllerView() {

    private var controller: LayoutVideoControllerBinding? = null
    private var setting: IMediaSetting? = null
    private var videoView: VideoView? = null

    override fun attachPlayer(player: IMediaController, setting: IMediaSetting) {
        super.attachPlayer(player, setting)
        if (player !is VideoView) {
            throw  UnsupportedOperationException()
        }
        videoView = player
        this.setting = setting
        if (controller == null) {
            controller = DataBindingUtil.inflate(
                LayoutInflater.from(player.context),
                R.layout.layout_video_controller,
                null,
                false
            )
        }
        controller?.apply {
            root.visibility = View.GONE
            if (!player.contains(root)) {
                player.addView(root, ViewGroup.LayoutParams(player.width, player.height))
            }
            controllerPlay.setOnClickListener { toggle() }
            controllerProgressSb.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    seekBar ?: return
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    pause()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar ?: return
                    seekTo(seekBar.progress)
                    start()
                }
            })
            /*root.setOnClickListener { showController() }*/
        }
        player.setOnClickListener {
            if (controller?.root?.isVisible == true) {
                /*hideController()*/
            } else {
                controller?.root?.visibility = View.VISIBLE
            }
        }
    }

    override fun onStart(timeout: Long) {
    }

    override fun onPause() {
    }

    override fun onStop() {
    }

    override fun startPrepare() {
    }

    override fun showInfo(info: MediaMate) {
    }

    override fun onPrepared() {
    }

    override fun onComplete() {
    }

    override fun onVideoViewSizeChanged(videoView: IMediaController, w: Int, h: Int) {
    }

    override fun onSettingChange(setting: IMediaSetting) {
        this.setting = setting
    }

}