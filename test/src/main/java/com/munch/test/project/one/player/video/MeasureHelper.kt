package com.munch.test.project.one.player.video

import android.view.View
import java.lang.ref.WeakReference

/**
 * Create by munch1182 on 2021/6/2 15:50.
 */
class MeasureHelper(view: View) {

    private val weakView = WeakReference(view)
    private var videoWidth = 0
    private var videoHeight = 0
    private var aspectRatio = 0

    fun getView() = weakView.get()

    fun setVideoSize(width: Int, height: Int) {
        videoWidth = width
        videoHeight = height
    }

    fun setAspectRatio(aspectRatio: Int) {
        this.aspectRatio = aspectRatio
    }
}