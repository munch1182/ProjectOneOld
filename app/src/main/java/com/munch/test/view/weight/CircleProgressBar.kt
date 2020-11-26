package com.munch.test.view.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.munch.lib.log.LogLog
import com.munch.test.R

/**
 * Create by munch on 2020/9/4 9:23
 */
class CircleProgressBar(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var progress = 0f
    private var colorBorder: Int
    private var colorComplete: Int
    private var colorText: Int
    private var textSize: Float
    private var startAngle: Int
    private var strokeWidth: Float
    private var radius = 0f

    private val paintCircle = Paint()
    private val paintText = Paint()
    private val rectF = RectF()


    init {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar)
        colorBorder = attributes.getColor(
            R.styleable.CircleProgressBar_colorCircleBorder, Color.parseColor("#e7e7e7")
        )
        colorComplete = attributes.getColor(
            R.styleable.CircleProgressBar_colorCircleComplete,
            Color.parseColor("#dd7c31")
        )
        colorText = attributes.getColor(
            R.styleable.CircleProgressBar_colorText, colorComplete
        )
        startAngle = attributes.getInt(R.styleable.CircleProgressBar_startAngle, 120)
        if (startAngle < 0 || startAngle > 360) {
            startAngle = 120
        }
        strokeWidth = attributes.getDimension(R.styleable.CircleProgressBar_strokeWidth, 20f)
        progress = attributes.getFloat(R.styleable.CircleProgressBar_progress, 0f)
        textSize = attributes.getDimension(R.styleable.CircleProgressBar_cpb_text_size, 130f)
        attributes.recycle()

        paintCircle.style = Paint.Style.STROKE
        paintCircle.isAntiAlias = true
        paintCircle.strokeWidth = strokeWidth
        //圆弧圆角
        paintCircle.strokeCap = Paint.Cap.ROUND
        paintCircle.strokeJoin = Paint.Join.ROUND

        paintText.color = colorText
        paintText.style = Paint.Style.FILL
        paintText.textSize = textSize
        paintText.textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec)*/
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val min = width.coerceAtMost(height)
        setMeasuredDimension(min, min)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        radius = w.coerceAtMost(h) / 2f
        val minSize = radius / 3
        if (textSize > minSize) {
            textSize = minSize
            paintText.textSize = textSize
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paintCircle.color = colorBorder
        canvas?.drawCircle(radius, radius, radius - strokeWidth, paintCircle)

        //计算文字基准线到中心的距离
        val fontMetrics = paintText.fontMetrics
        val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        canvas?.drawText(
            "".plus(progress.toInt()).plus(" %"),
            radius,
            radius + distance,
            paintText
        )

        paintCircle.color = colorComplete
        rectF.set(
            strokeWidth,
            strokeWidth,
            radius * 2 - strokeWidth,
            radius * 2 - strokeWidth
        )
        canvas?.drawArc(
            rectF,
            startAngle.toFloat(),
            (progress / 100f) * 360f,
            false,
            paintCircle
        )
    }

    fun setProgress(progress: Float) {
        when {
            progress < 0 -> {
                this.progress = 0f
            }
            progress > 100 -> {
                this.progress = 100f
            }
            else -> {
                this.progress = progress
            }
        }
        postInvalidate()
    }

    fun getProgress(): Float {
        return this.progress
    }

    fun setColorBorder(color: Int): CircleProgressBar {
        this.colorBorder = color
        return this
    }

    fun setColorComplete(color: Int): CircleProgressBar {
        this.colorComplete = color
        return this
    }

    fun setColorText(color: Int): CircleProgressBar {
        this.colorText = color
        return this
    }

    fun setTextSize(textSize: Float): CircleProgressBar {
        this.textSize = textSize
        return this
    }

    fun setStartAngle(startAngle: Int): CircleProgressBar {
        this.startAngle = startAngle
        return this
    }

    fun setStrokeWidth(strokeWidth: Float): CircleProgressBar {
        this.strokeWidth = strokeWidth
        return this
    }

}