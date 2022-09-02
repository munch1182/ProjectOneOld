package com.munch.lib.weight.progressbar

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * 处理除UI外的Progressbar逻辑
 */
abstract class AbsProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes)