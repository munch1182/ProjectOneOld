package com.munch.lib.android.extend

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint

/**
 * Create by munch1182 on 2022/3/12 14:32.
 */

/**
 * 返回一个红色、绘制虚线的Paint
 */
fun testPaint(): Lazy<Paint> {
    return lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            pathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 0f)
            color = Color.RED
        }
    }
}

/**
 * 绘制Rect的边线
 */
fun Canvas.drawRectLine(l: Float, t: Float, r: Float, b: Float, paint: Paint) {
    drawLines(floatArrayOf(l, t, r, t, r, t, r, b, r, b, l, b, l, b, l, t), paint)
}