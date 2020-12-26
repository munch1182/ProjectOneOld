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
class TeardropView : View {

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
        angle = attributes.getDimension(R.styleable.TeardropView_teardrop_angle, 45f).toInt()
        val textSize = attributes.getDimension(R.styleable.TeardropView_teardrop_text_size, 80f)
        attributes.recycle()
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                this.textSize = textSize
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

    private val paint: Paint
    private val path = Path()
    private var bgColor = Color.GREEN
    private var textColor = Color.WHITE
    private var angle = 45
    private var text = ""

    fun setAngle(angle: Int) {
        if (this.angle != angle) {
            this.angle = angle
            invalidate()
        }
    }

    fun setText(str: String?) {
        val text = str ?: ""
        if (text != this.text) {
            this.text = text
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        val min = min(
            measuredHeight - paddingBottom - paddingTop,
            measuredWidth - paddingLeft - paddingRight
        )
        val radius = min / 2f

        paint.color = bgColor
        canvas.drawCircle(radius, radius, radius, paint)

        setPathByAngle(radius)
        canvas.drawPath(path, paint)

        paint.color = textColor
        drawTextInCenter(canvas, radius, radius)
    }

    private fun drawTextInCenter(canvas: Canvas, cx: Float, cy: Float) {
        val textWidth = paint.measureText(text)
        val fontMetrics = paint.fontMetrics
        val baseLineY = cy + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        canvas.drawText(text, cx - textWidth / 2, baseLineY, paint)
    }

    /**
     * todo 需要数学去完成[setAngle]
     */
    private fun setPathByAngle(radius: Float) {
        val conner = 30f
        path.reset()
        path.moveTo(radius, 0f)
        path.lineTo(radius * 2 - conner, 0f)
        path.quadTo(radius * 2, 0f, radius * 2, conner)
        path.lineTo(radius * 2, radius)
        path.close()
    }
}