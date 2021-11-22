package com.munch.lib.base

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import kotlin.math.max

/**
 * Create by munch1182 on 2021/11/12 10:38.
 */
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
    val baseLineY = cy + (fm.bottom - fm.top) / 2f - fm.bottom
    drawText(text, cx, baseLineY, paint)
}

fun Paint.measureTextBounds(text: String, bound: Rect = Rect()): Rect {
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
        maxWidth = max(rect.width(), maxWidth)
        maxHeight = max(rect.height(), maxHeight)
    }
    return Pair(maxWidth, maxHeight)
}