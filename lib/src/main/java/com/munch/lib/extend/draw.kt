@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * Create by munch1182 on 2022/3/12 14:32.
 */

/**
 * 返回一个红色、绘制虚线的Paint
 */
fun testPaint(): Lazy<Paint> {
    return lazy { Paint(Paint.ANTI_ALIAS_FLAG).setDashPath() }
}

inline fun Paint.setDashPath(
    color: Int = Color.RED,
    dash: PathEffect = DashPathEffect(floatArrayOf(10f, 5f, 10f, 5f), 0f)
): Paint {
    pathEffect = dash
    setColor(color)
    style = Paint.Style.STROKE
    return this
}

/**
 * 绘制Rect的边线
 */
inline fun Canvas.drawRectLine(l: Float, t: Float, r: Float, b: Float, paint: Paint) {
    drawLines(floatArrayOf(l, t, r, t, r, t, r, b, r, b, l, b, l, b, l, t), paint)
}

inline fun Canvas.drawRectLint(rect: RectF, paint: Paint) =
    drawRectLine(rect.left, rect.top, rect.right, rect.bottom, paint)

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

fun MotionEvent.toStr() = this.let {
    val act = when (it.action) {
        MotionEvent.ACTION_DOWN -> "down"
        MotionEvent.ACTION_UP -> "up"
        MotionEvent.ACTION_MOVE -> "move"
        MotionEvent.ACTION_CANCEL -> "cancel"
        else -> it.action
    }
    "$act(${it.x},${it.y})"
}

fun PointF.isMove(point: PointF, dis: Float = 25f) =
    abs(x - point.x) > dis || abs(y - point.y) > dis