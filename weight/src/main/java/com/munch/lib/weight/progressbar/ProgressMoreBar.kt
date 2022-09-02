package com.munch.lib.weight.progressbar

import android.content.Context
import android.util.AttributeSet

/**
 * 支持ProgressBar, 但只支持横向有数值的进度
 * 支持ProgressBar最小值最大值label字符的设置, 并支持显示位置(左右两侧/上/下)
 * 支持进度条颜色渐变/分段
 * 支持进度条部分不可用(选中该部分时直接跳过)
 */
class ProgressMoreBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : AbsProgressBar(context, attrs, defStyleAttr, defStyleRes)