@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.graphics.*
import android.view.View
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
    style = Paint.Style.STROKE
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
    p.moveTo(0f, 15f)
    rotate(-60f)
    p.moveTo(0f, 15f)
    rotate(30f)
    p.lineTo(sx, sy)
    paint.setDashPath()
    drawPath(p, paint)

    restore()
}

fun Rect.adjust(l: Int, t: Int, r: Int, b: Int) {
    left += l
    top += t
    right += r
    bottom += b
}

inline fun Rect.fitViewPadding(view: View, fitTopBottom: Boolean = true) =
    adjust(
        view.paddingLeft,
        if (fitTopBottom) view.paddingTop else 0,
        -view.paddingRight,
        if (fitTopBottom) -view.paddingBottom else 0,
    )

/**
 * rect水平方向平移[r]的距离，垂直方向平移[b]的距离
 */
inline fun Rect.translation(r: Int, b: Int) = adjust(r, b, r, b)

//<editor-fold desc="move2">
/**
 * 将rect的left移动向[left]点，rect的right会跟随移动，且宽度会保持不变
 */
inline fun Rect.moveLeftTo(left: Int) {
    val width = width()
    this.left = left
    this.right = left + width
}

inline fun Rect.moveRightTo(right: Int) {
    val width = width()
    this.right = right
    this.left = right - width
}

inline fun Rect.moveTopTo(top: Int) {
    val height = height()
    this.top = top
    this.bottom = top + height
}

inline fun Rect.moveBottomTo(bottom: Int) {
    val height = height()
    this.bottom = bottom
    this.top = bottom - height
}

inline fun Rect.moveTo(left: Int, top: Int) {
    moveLeftTo(left)
    moveTopTo(top)
}
//</editor-fold>

//<editor-fold desc="copy">
inline fun Rect.copyLR(rect: Rect) {
    left = rect.left
    right = rect.right
}

inline fun Rect.copyTB(rect: Rect) {
    top = rect.top
    bottom = rect.bottom
}
//</editor-fold>

fun RectF.adjust(l: Float, t: Float, r: Float, b: Float) {
    left += l
    top += t
    right += r
    bottom += b
}

inline fun RectF.fitViewPadding(view: View, fitTopBottom: Boolean = true) =
    adjust(
        view.paddingLeft.toFloat(),
        if (fitTopBottom) view.paddingTop.toFloat() else 0f,
        -view.paddingRight.toFloat(),
        if (fitTopBottom) -view.paddingBottom.toFloat() else 0f,
    )

/**
 * rect水平方向平移[r]的距离，垂直方向平移[b]的距离
 */
inline fun RectF.translation(r: Float, b: Float) = adjust(r, b, r, b)

inline fun PointF.isIn(rectF: RectF): Boolean {
    return x in rectF.left..rectF.right && y in rectF.top..rectF.bottom
}

//<editor-fold desc="move2">
/**
 * 将rect的left移动向[left]点，rect的right会跟随移动，且宽度会保持不变
 */
inline fun RectF.moveLeftTo(left: Float) {
    val width = width()
    this.left = left
    this.right = left + width
}

inline fun RectF.moveRightTo(right: Float) {
    val width = width()
    this.right = right
    this.left = right - width
}

inline fun RectF.moveTopTo(top: Float) {
    val height = height()
    this.top = top
    this.bottom = top + height
}

inline fun RectF.moveBottomTo(bottom: Float) {
    val height = height()
    this.bottom = bottom
    this.top = bottom - height
}
//</editor-fold>

//<editor-fold desc="copy">
inline fun RectF.copyLR(rect: RectF) {
    left = rect.left
    right = rect.right
}

inline fun RectF.copyTB(rect: RectF) {
    top = rect.top
    bottom = rect.bottom
}

inline fun RectF.moveTo(left: Float, top: Float) {
    moveLeftTo(left)
    moveTopTo(top)
}
//</editor-fold>
