package com.munch.test.project.one.player.media

import android.content.Context
import android.util.AttributeSet

/**
 * Create by munch1182 on 2021/5/31 11:58.
 */
class VideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttr: Int = 0
) : PlayerView(context, attrs, defAttr) {

    init {
        initPlayer()
    }
}