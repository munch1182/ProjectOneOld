package com.munch.lib.weight

import android.graphics.RectF
import android.view.View
import kotlin.math.min

/**
 * Create by munch1182 on 2021/11/12 09:27.
 */
interface CircleViewHelper {

    val defWidth: Int
    val rect: RectF

    /**
     * circleWidth
     */
    val cw: Int
        get() = 0

    /**
     * 根据设置确定正方形大小
     */
    fun measureRadius(widthMeasureSpec: Int, heightMeasureSpec: Int): Int {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val width = if (widthMode != View.MeasureSpec.EXACTLY) {
            defWidth
        } else {
            View.MeasureSpec.getSize(widthMeasureSpec)
        }
        val height = if (heightMode != View.MeasureSpec.EXACTLY) {
            defWidth
        } else {
            View.MeasureSpec.getSize(heightMeasureSpec)
        }
        return min(width, height)
    }

    /**
     * 根据最后的宽高来填充[rect]对象
     */
    fun measureRect(view: View, w: Int, h: Int) {
        val half = cw
        rect.set(
            view.paddingLeft.toFloat() + half,
            view.paddingTop.toFloat() + half,
            (w - view.paddingRight).toFloat() - half,
            (h - view.paddingBottom).toFloat() - half
        )
    }

}