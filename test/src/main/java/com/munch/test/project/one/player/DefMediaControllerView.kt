package com.munch.test.project.one.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.contains
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.munch.pre.lib.extend.setParams
import com.munch.pre.lib.log.log
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
    private var allStr = ""

    companion object {
        private const val EIGHT_HOUR: Long = 8 * 60 * 60 * 1000
    }

    override fun attachVideo(videoView: VideoView, setting: MediaSetting) {
        if (controller == null) {
            controller = DataBindingUtil.inflate(
                LayoutInflater.from(videoView.context),
                R.layout.layout_video_controller,
                null,
                false
            )
        }
        allStr = ""
        controller?.apply {
            root.visibility = View.GONE
            if (!videoView.contains(root)) {
                videoView.addView(root, ViewGroup.LayoutParams(videoView.width, videoView.height))
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
                    showProgressBySb(seekBar.progress)
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
            root.setOnClickListener { showController() }
        }
        videoView.setOnClickListener {
            if (controller?.root?.isVisible == true) {
                hideController()
            } else {
                controller?.root?.visibility = View.VISIBLE
            }
        }
        this.setting = setting
        this.videoView = videoView
    }

    override fun onVideoViewSizeChanged(videoView: VideoView, w: Int, h: Int) {
        controller?.root?.setParams {
            width = w
            height = h
        }
    }

    override fun onStart(timeout: Long) {
        log("view onStart")
        requestProgress()
    }

    override fun onPause() {
    }

    override fun onStop() {
    }

    override fun startPrepare() {
        controller?.apply {
            root.visibility = View.VISIBLE
            root.postDelayed({ hideController() }, 1000L)
            controllerProgressTv.text = formatProgress()
        }
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
        controller?.apply { controllerProgressSb.max = duration }
    }

    override fun onComplete() {
        controller?.apply {
            controllerProgressSb.progress = duration
            controllerProgressTv.text = formatProgress(getAllStr(), getAllStr())
            root.visibility = View.VISIBLE
        }
    }

    private fun formatProgress(progress: String = "00:00", allStr: String = "00:00") =
        "$progress/$allStr"

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

    private fun showProgress(offset: Int = 0) {
        controller?.apply {
            controllerProgressTv.text = formatProgress(getCurrentStr(offset), getAllStr())
            controllerProgressSb.progress = currentPosition
        }
    }

    private fun showProgressBySb(current: Int) {
        controller?.apply {
            controllerProgressTv.text = formatProgress(getCurrentStr(current), getAllStr())
        }
    }

    private fun getAllStr(): String {
        //只适配了东八区时间
        val str = String.format("%tT", duration.toLong() - EIGHT_HOUR)
        if (str.startsWith("00:")) {
            return str.substring(3)
        }
        return str
    }

    private fun getCurrentStr(current: Int = currentPosition): String {
        //只适配了东八区时间
        val str = String.format("%tT", (current).toLong() - EIGHT_HOUR)
        if (str.startsWith("00:")) {
            return str.substring(3)
        }
        return str

    }

    override fun settingChange(setting: MediaSetting) {
        this.setting = setting
    }
}