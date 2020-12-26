package com.munch.project.test.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.munch.project.test.R
import kotlin.math.min

/**
 * Create by munch1182 on 2020/12/25 11:56.
 */
open class TeardropView : View {

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
    ) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.TeardropView)
        bgColor = attributes.getColor(R.styleable.TeardropView_teardrop_color, Color.GREEN)
        text = attributes.getString(R.styleable.TeardropView_teardrop_text) ?: ""
        textColor =
            attributes.getColor(R.styleable.TeardropView_teardrop_text_color, Color.WHITE)
        angle = attributes.getInt(R.styleable.TeardropView_teardrop_angle, 45)
        radius = attributes.getDimension(R.styleable.TeardropView_teardrop_radius, -1f)
            .takeIf { it != -1f }
        corner = attributes.getDimension(R.styleable.TeardropView_teardrop_corner, 30f)
        textSize = attributes.getDimension(R.styleable.TeardropView_teardrop_text_size, 80f)
        attributes.recycle()
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                this.textSize = this@TeardropView.textSize
            }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    internal val paint: Paint
    internal val path = Path()
    var bgColor = Color.BLUE
    var textColor = Color.WHITE
    var angle = 45
        set(value) {
            field = if (value > 360) {
                value - 360
            } else {
                value
            }
        }
    var text = ""
    var radius: Float? = null
    var corner: Float = 30f
    var textSize: Float = 80f

    fun setProperty(func: TeardropView.() -> Unit) {
        func.invoke(this)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        val (radius, cx, cy) = sureCircleParameter()

        paint.color = bgColor
        canvas.drawCircle(cx, cy, radius, paint)

        surePathByAngle(cx, cy, radius)
        canvas.drawPath(path, paint)

        paint.color = textColor
        drawTextInCenter(canvas, cx, cy)
    }

    open fun sureCircleParameter(): Triple<Float, Float, Float> {
        val endHeight = measuredHeight - paddingBottom - paddingTop
        val endWidth = measuredWidth - paddingLeft - paddingRight
        val min = min(endHeight, endWidth)

        val radius = this.radius ?: min / 2f

        val cx = endHeight / 2f
        val cy = endWidth / 2f
        return Triple(radius, cx, cy)
    }

    open fun drawTextInCenter(canvas: Canvas, cx: Float, cy: Float) {
        val textWidth = paint.measureText(text)
        val fontMetrics = paint.fontMetrics
        val baseLineY = cy + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        canvas.drawText(text, cx - textWidth / 2, baseLineY, paint)
    }

    open fun surePathByAngle(cx: Float, cy: Float, radius: Float) {

        val isTop = angle < 90 || angle > 270
        val isRight = angle in 1 until 180

        val startX: Float = cx
        val startY = if (isTop) cy - radius else cy + radius

        val endX = if (isRight) cx + radius else cx - radius
        val endY: Float = cy


        path.reset()
        path.moveTo(startX, startY)
        val cornerX = if (isRight) endX - corner else endX + corner
        path.lineTo(cornerX, startY)
        val cornerY = if (isTop) startY + corner else startY - corner
        path.quadTo(endX, startY, endX, cornerY)
        path.lineTo(endX, endY)
        path.close()
    }

}