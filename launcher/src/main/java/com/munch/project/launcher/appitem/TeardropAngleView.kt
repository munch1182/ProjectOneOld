package com.munch.project.launcher.appitem

import android.content.Context
import android.util.AttributeSet
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Create by munch1182 on 2020/12/25 11:56.
 */
class TeardropAngleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : TeardropView(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {

    companion object {
        private const val PI = 3.14
    }

    override fun sureCircleParameter(): Triple<Float, Float, Float> {
        val endHeight = measuredHeight - paddingBottom - paddingTop
        val endWidth = measuredWidth - paddingStart - paddingEnd
        val min = min(endHeight, endWidth)

        val radius = this.radius ?: min * 2f / 5f

        var cx = min / 2f + (endWidth - min) / 2f + paddingStart
        var cy = min / 2f + (endHeight - min) / 2f + paddingTop

        if (angle % 90 != 0) {
            return Triple(radius, cx, cy)
        }
        when (angle) {
            0 -> {
                cx -= radius / 5
            }
            90 -> {
                cy -= radius / 5
            }
            180 -> {
                cx += radius / 5
            }
            270 -> {
                cy += radius / 5
            }
        }
        return Triple(radius, cx, cy)
    }

    override fun surePathByAngle(cx: Float, cy: Float, radius: Float) {

        val startX = getPosXInCircle(changeAngle(angle, -45), cx, radius)
        val startY = getPosYInCircle(changeAngle(angle, -45), cy, radius)

        val radiusDis = radius * 3 / 2
        val centerX = getPosXInCircle(angle, cx, radiusDis)
        val centerY = getPosYInCircle(angle, cy, radiusDis)

        val endX = getPosXInCircle(changeAngle(angle, 45), cx, radius)
        val endY = getPosYInCircle(changeAngle(angle, 45), cy, radius)

        path.reset()
        path.moveTo(startX, startY)
        when (angle) {
            0 -> {
                path.lineTo(centerX - corner, centerY - corner)
                path.quadTo(centerX, centerY, centerX - corner, centerY + corner)
            }
            45 -> {
                path.lineTo(centerX, centerY - corner)
                path.quadTo(centerX, centerY, centerX - corner, centerY)
            }
            90 -> {
                path.lineTo(centerX + corner, centerY - corner)
                path.quadTo(centerX, centerY, centerX - corner, centerY - corner)
            }
            135 -> {
                path.lineTo(centerX + corner, centerY)
                path.quadTo(centerX, centerY, centerX, centerY - corner)
            }
            180 -> {
                path.lineTo(centerX + corner, centerY + corner)
                path.quadTo(centerX, centerY, centerX + corner, centerY - corner)
            }
            225 -> {
                path.lineTo(centerX, centerY + corner)
                path.quadTo(centerX, centerY, centerX + corner, centerY)
            }
            270 -> {
                path.lineTo(centerX - corner, centerY + corner)
                path.quadTo(centerX, centerY, centerX + corner, centerY + corner)
            }
            315 -> {
                path.lineTo(centerX - corner, centerY)
                path.quadTo(centerX, centerY, centerX, centerY + corner)
            }
        }
        path.lineTo(endX, endY)
        path.close()
    }

    private fun getPosYInCircle(angle: Int, cy: Float, radius: Float) =
        (cy + radius * sin(angle * PI / 180f)).toFloat()

    private fun getPosXInCircle(angle: Int, cx: Float, radius: Float) =
        (cx + radius * cos(angle * PI / 180f)).toFloat()

    private fun changeAngle(angle: Int, add: Int): Int {
        val value = angle + add
        val i = value.absoluteValue / 360
        return when {
            value > 0 -> {
                value - 360 * i
            }
            value < 0 -> {
                value + 360 * (i + 1)
            }
            else -> {
                0
            }
        }
    }

}