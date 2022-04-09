@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

import android.graphics.*
import java.lang.Math.max
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.atan2

/**
 * Create by munch1182 on 2022/3/12 14:32.
 */

/**
 * 返回一个红色、绘制虚线的Paint
 */
fun testPaint(): Lazy<Paint> {
    return lazy { Paint(Paint.ANTI_ALIAS_FLAG).setDashPath() }
}

inline fun Paint.setDashPath(color: Int = Color.RED): Paint {
    pathEffect = DashPathEffect(floatArrayOf(10f, 5f, 10f, 5f), 0f)
    setColor(color)
    return this
}

/**
 * 绘制Rect的边线
 */
fun Canvas.drawRectLine(l: Float, t: Float, r: Float, b: Float, paint: Paint) {
    drawLines(floatArrayOf(l, t, r, t, r, t, r, b, r, b, l, b, l, b, l, t), paint)
}

/**
 * 将text从cx右，cy下开始绘制
 */
fun Canvas.drawTextInTop(text: String, cx: Float, cy: Float, paint: Paint) {
    val fm = paint.fontMetrics
    val baseLineY = cy + -(fm.ascent - fm.descent) / 2f
    drawText(text, cx, baseLineY, paint)
}

/**
 * 将cx,cy作为中心点绘制文字
 */
fun Canvas.drawTextInCenter(text: String, cx: Float, cy: Float, paint: Paint) {
    val textWidth = paint.measureText(text)
    val fm = paint.fontMetrics
    val baseLineY = cy + (fm.bottom - fm.top) / 2f - fm.bottom
    drawText(text, cx - textWidth / 2f, baseLineY, paint)
}

/**
 * 将cx作为起点,将cy作为中心高度绘制文字
 */
fun Canvas.drawTextInYCenter(text: String, cx: Float, cy: Float, paint: Paint) {
    val fm = paint.fontMetrics
    val baseLineY = cy + ((fm.bottom - fm.top) / 2f - fm.bottom)
    drawText(text, cx, baseLineY, paint)
}

inline fun Paint.measureTextBounds(text: String, bound: Rect = Rect()): Rect {
    getTextBounds(text, 0, text.length, bound)
    return bound
}

/**
 * 获取该列表文字的最大宽度和高度
 */
fun Paint.measureMaxTextSpace(array: Array<String>): Pair<Int, Int> {
    if (array.isEmpty()) {
        return Pair(0, 0)
    }
    val rect = Rect()
    var maxWidth = 0
    var maxHeight = 0
    array.forEach {
        getTextBounds(it, 0, it.length, rect)
        maxWidth = rect.width().coerceAtLeast(maxWidth)
        maxHeight = rect.height().coerceAtLeast(maxHeight)
    }
    return Pair(maxWidth, maxHeight)
}

fun Canvas.drawGuidLine(
    cx: Float,
    cy: Float,
    sx: Float,
    sy: Float,
    text: String?,
    paint: Paint,
    path: Path? = null
) {
    save()

    val p = path ?: Path()
    p.reset()

    val w = sx.absoluteValue.toDouble()
    val h = sy.absoluteValue.toDouble()
    val angle = atan2(w, h) * 180 / Math.PI

    rotate(angle.toFloat())
    p.moveTo(0f, 0f)
    rotate(30f)
    p.moveTo(0f,15f)
    rotate(-60f)
    p.moveTo(0f,15f)
    rotate(30f)
    p.lineTo(sx, sy)
    paint.setDashPath()
    drawPath(p, paint)

    restore()
}