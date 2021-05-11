package com.munch.test.project.one.player

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.core.view.contains
import androidx.databinding.DataBindingUtil
import com.munch.pre.lib.extend.ViewHelper
import com.munch.test.project.one.R
import com.munch.test.project.one.databinding.LayoutVideoControllerBinding
import com.munch.test.project.one.player.media.MediaControllerView
import com.munch.test.project.one.player.media.MediaSetting
import com.munch.test.project.one.player.media.VideoView


/**
 * Create by munch1182 on 2021/5/11 14:41.
 */
class DefMediaControllerView : MediaControllerView() {

    private var controller: LayoutVideoControllerBinding? = null
    private var setting: MediaSetting? = null
    private var videoView: VideoView? = null

    override fun attachVideo(videoView: VideoView, setting: MediaSetting) {
        if (controller == null) {
            controller = DataBindingUtil.inflate(
                LayoutInflater.from(videoView.context),
                R.layout.layout_video_controller,
                null,
                false
            )
        }
        controller?.apply {
            root.visibility = View.GONE
            if (!videoView.contains(root)) {
                videoView.addView(root, ViewHelper.newParamsMM())
            }
            controller = this@DefMediaControllerView
            controllerPlay.setOnClickListener { toggle() }
            controllerProgressSb.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar ?: return
                    controller?.seekTo(seekBar.progress)
                }
            })
            root.setOnClickListener { showController() }
        }
        this.setting = setting
        this.videoView = videoView
    }

    override fun onStart(timeout: Long) {
        requestProgress()
    }

    override fun onPause() {
    }

    override fun onStop() {
    }

    @SuppressLint("SetTextI18n")
    override fun startPrepare() {
        controller?.apply {
            root.visibility = View.VISIBLE
            controllerProgressTv.text = "00:00/00:00"
        }
    }

    override fun toggle() {
        /*super.toggle()*/
        if (!isPlaying) {
            requestProgress()
        }
        videoView?.toggle()
    }

    private fun requestProgress() {
        if (!isPlaying) {
            return
        }
        controller?.apply {
            showProgress()
            root.postDelayed({ requestProgress() }, 1000L)
        }
    }

    override fun onPrepared() {
        showController()
        showProgress()
        controller?.apply { controllerProgressSb.max = getAll() }
    }

    private fun showController() {
        controller?.apply {
            if (controllerTop.isShown) {
                return
            }
            controllerTop.visibility = View.VISIBLE
            root.visibility = View.VISIBLE
            root.postDelayed({ hideController() }, 1000L)
        }
    }

    private fun hideController() {
        controller?.apply {
            if (setting?.keepProgress == true) {
                controllerTop.visibility = View.GONE
            } else {
                root.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showProgress() {
        controller?.apply {
            controllerProgressTv.text = "${getCurrent()}/${getAll()}"
        }
    }

    private fun getAll() = videoView!!.duration / 1000

    private fun getCurrent() = videoView!!.currentPosition / 1000 * 100

    override fun settingChange(setting: MediaSetting) {
        this.setting = setting
    }
}