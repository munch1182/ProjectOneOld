package com.munch.lib.android.weight

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * 占位控件，具体占位意义由其父控件决定
 *
 * Create by munch1182 on 2022/3/9 10:49.
 */
class Sign @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : View(context, attrs, styleDef) {

    init {
        visibility = GONE
        setWillNotDraw(true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(0, 0)
    }
}