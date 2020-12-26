package com.munch.project.test.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Create by munch1182 on 2020/12/25 11:56.
 */
class TeardropAngleView : TeardropView {

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    companion object {
        private const val PI = 3.14

    }

    override fun sureCircleParameter(): Triple<Float, Float, Float> {
        val endHeight = measuredHeight - paddingBottom - paddingTop
        val endWidth = measuredWidth - paddingLeft - paddingRight
        val min = min(endHeight, endWidth)

        val radius = this.radius ?: min / 3f

        val cx = endHeight / 2f
        val cy = endWidth / 2f
        return Triple(radius, cx, cy)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun surePathByAngle(cx: Float, cy: Float, radius: Float) {

        val startX = getPosXInCircle(angle, cx, radius)
        val startY = getPosYInCircle(angle, cy, radius)

        val radiusDis = radius * 3 / 2
        val centerX = getPosXInCircle(angle + 45, cx, radiusDis)
        val centerY = getPosYInCircle(angle + 45, cy, radiusDis)

        val endX = getPosXInCircle(angle + 90, cx, radius)
        val endY = getPosYInCircle(angle + 90, cy, radius)

        path.reset()
        path.moveTo(startX, startY)
        /*path.lineTo(cornerSX, cornerSY)
        path.quadTo(centerX, centerY, cornerSX, cornerSY)*/
        path.lineTo(endX, endY)
        path.close()
    }

    private fun getPosYInCircle(angle: Int, cy: Float, radius: Float) =
        (cy + radius * sin(angle * PI / 180f)).toFloat()

    private fun getPosXInCircle(angle: Int, cx: Float, radius: Float) =
        (cx + radius * cos(angle * PI / 180f)).toFloat()

}