package com.munch.lib.weight

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

/**
 * 用于流布局
 * 目标：
 * 1. 可以设置对齐方式(start,end,center,center_vertical,center_horizontal,end_center_vertical)
 * 2. 可以设置每行最大个数
 * 3. 可以设置间隔
 *
 * Create by munch1182 on 2021/8/10 17:33.
 */
class FlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : ViewGroup(context, attrs, styleDef) {

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

    }

}