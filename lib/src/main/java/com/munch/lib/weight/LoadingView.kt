package com.munch.lib.weight

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.munch.lib.R
import com.munch.lib.base.dp2Px

/**
 * Create by munch1182 on 2021/11/12 14:42.
 */
class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : View(context, attrs, styleDef), CircleViewHelper, ViewUpdate<LoadingView> {

    override val defWidth = context.dp2Px(32f).toInt()
    override val cw: Int
        get() = circleWidth.toInt()
    override val rect = RectF()
    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
            strokeWidth = circleWidth
            gradient?.let {
                val half = rect.width() / 2f
                gradientMatrix.setTranslate(half, half)
                it.setLocalMatrix(gradientMatrix)
                shader = it
            }
        }
    }
    private val gradientMatrix by lazy { Matrix() }
    private var rotate = 30f
        set(value) {
            field = if (value > 360) (value - 360) else value
        }

    var circleWidth = context.dp2Px(4f)
    var startColor = Color.WHITE
    var endColor = Color.parseColor("#28FFFFFF")
    var speed = 16L

    //开始颜色和结束颜色要反向设置，因为转的方向的问题
    var gradient: SweepGradient? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.LoadingView).apply {
            circleWidth = getDimension(R.styleable.LoadingView_strokeWidth, circleWidth)
            speed = getInt(R.styleable.LoadingView_lvSpeed, speed.toInt()).toLong()
            startColor = getColor(R.styleable.LoadingView_lvStartColor, startColor)
            endColor = getColor(R.styleable.LoadingView_lvEndColor, endColor)
            gradient = SweepGradient(0f, 0f, endColor, startColor)
        }.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val radius = measureRadius(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(radius, radius)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        measureRect(this, w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        canvas.rotate(rotate, rect.centerX(), rect.centerY())
        canvas.drawArc(rect, 0f, 280f, false, paint)

        rotate += 10
        postInvalidateDelayed(speed)
    }

    override fun set(set: LoadingView.() -> Unit) {
        super.set(set)
        invalidate()
    }
}