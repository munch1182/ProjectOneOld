package com.munch.test.project.one.player.video

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View

/**
 * Create by munch1182 on 2021/6/2 15:43.
 */
interface IRenderView {

    companion object {
        /**
         * 视频显示模式：View 是视频画面能显示的控件
         * 0 -> 填满View一边，另一边按比例缩小，整个视频显示在View里面
         * 1 -> 整个填满View，可能有一边会被裁剪一部分画面
         * 2 -> 根据原始视频大小来测量，不会超出View范围
         * 3 -> 按View的原始测量值来设定
         * 4 -> 按16：9的比例来处理，不会超出View范围
         * 5 -> 按4：3的比例来处理，不会超出View范围
         */
        const val AR_ASPECT_FIT_PARENT = 0 // without clip
        const val AR_ASPECT_FILL_PARENT = 1 // may clip
        const val AR_ASPECT_WRAP_CONTENT = 2
        const val AR_MATCH_PARENT = 3
        const val AR_16_9_FIT_PARENT = 4
        const val AR_4_3_FIT_PARENT = 5
    }

    val measureHelper: MeasureHelper

    fun getHolder(): SurfaceHolder?

    fun getView(): View {
        if (this is View) {
            return this
        } else {
            throw UnsupportedOperationException()
        }
    }

    fun bindPlayer(player: IPlayer) {
        measureHelper.setVideoSize(player.getVideoWidth(), player.getVideoHeight())
    }

    fun setAspectRatio(aspectRatio: Int) {
        measureHelper.setAspectRatio(aspectRatio)
    }
}

class SurfaceRenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttr: Int = 0
) : SurfaceView(context, attrs, defAttr), IRenderView {

    override val measureHelper = MeasureHelper(this)
}

class TextureRenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttr: Int = 0
) : TextureView(context, attrs, defAttr), IRenderView, TextureView.SurfaceTextureListener {

    override val measureHelper = MeasureHelper(this)
    override fun getHolder(): SurfaceHolder? = null

    init {
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

}
