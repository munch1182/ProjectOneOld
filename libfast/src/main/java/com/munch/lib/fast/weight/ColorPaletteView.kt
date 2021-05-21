package com.munch.lib.fast.weight

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt

/**
 * Create by munch1182 on 2021/5/19 16:45.
 */
class ColorPaletteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val saturationPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    fun setColor(@ColorInt color: Int) {
        paint.shader = null
        paint.color = color
        invalidate()
    }

    fun reset() {
        resetColor()
        invalidate()
    }

    private fun resetColor() {
        paint.shader = SweepGradient(
            centerX,
            centerY,
            intArrayOf(
                Color.RED,
                Color.MAGENTA,
                Color.BLUE,
                Color.CYAN,
                Color.GREEN,
                Color.YELLOW,
                Color.RED
            ),
            null
        )
        saturationPaint.shader = RadialGradient(
            centerX,
            centerY,
            radius,
            Color.WHITE,
            0x00ffffff,
            Shader.TileMode.CLAMP
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val width = w - paddingLeft - paddingRight
        val height = h - paddingTop - paddingBottom
        radius = width.coerceAtMost(height) * 0.5f
        centerX = w * 0.5f
        centerY = h * 0.5f

        resetColor()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(centerX, centerY, radius, paint)
        canvas?.drawCircle(centerX, centerY, radius, saturationPaint)
    }
}