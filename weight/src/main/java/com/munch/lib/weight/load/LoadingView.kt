package com.munch.lib.weight.load

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.munch.lib.extend.ViewUpdateListener
import com.munch.lib.graphics.RectF
import com.munch.lib.weight.IColorView
import com.munch.lib.weight.R
import com.munch.lib.weight.ViewHelper
import com.munch.lib.weight.ViewHelperDefault

class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes), ViewHelper by ViewHelperDefault,
    IColorView, ViewUpdateListener<LoadingView> {

    init {
        paint.apply {
            color = this@LoadingView.mainColor
            style = Paint.Style.FILL
        }
    }

    private val shapeRect = RectF()
    private var round = 0f
    private val center = PointF()

    var mainColor = Color.parseColor("#9C9C9F")
        set(value) {
            field = value
            shapeColor.clear()
            shapeColor.addAll(getShapeColor())
        }
    var shapeCount = 8
        set(value) {
            if (shapeCount < 1) {
                throw IllegalStateException()
            }
            field = value
            degrees = -(360f / shapeCount)
        }

    //旋转速度
    var speed = 80L
    var shapeAlpha = 0.12f
    var minShapeAlpha = 0.25f

    private var degrees = -(360f / shapeCount)
    private val shapeColor = getShapeColor()
    private var pos = 0

    init {
        context.obtainStyledAttributes(attrs, R.styleable.LoadingView).apply {
            speed = getInteger(R.styleable.LoadingView_speed, speed.toInt()).toLong()
            shapeAlpha = getFloat(R.styleable.LoadingView_shapeAlpha, shapeAlpha)
            shapeCount = getInteger(R.styleable.LoadingView_shapeCount, shapeCount)
            mainColor = getColor(R.styleable.LoadingView_android_color, mainColor)
        }.recycle()
    }

    private fun getShapeColor(): MutableList<Int> {
        val r = Color.red(mainColor)
        val g = Color.green(mainColor)
        val b = Color.blue(mainColor)
        val min = (minShapeAlpha * 255).toInt()
        return MutableList(shapeCount) {
            var a = (255 * (1 - it * shapeAlpha)).toInt()
            if (a < min) a = min
            Color.argb(a, r, g, b)
        }
    }

    override fun update(update: LoadingView.() -> Unit) {
        update.invoke(this)
        invalidate()
    }

    override fun setColor(color: Int) {
        update { mainColor = color }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var radius = getSquareRadius(widthMeasureSpec, heightMeasureSpec)
        if (radius == 0) {
            radius = 25
        }
        setMeasuredDimension(
            radius + paddingLeft + paddingRight,
            radius + paddingTop + paddingBottom
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutView(this, w, h)

        val rw = w - paddingLeft - paddingRight
        val rh = h - paddingTop - paddingBottom

        center.set(w / 2f, h / 2f)

        val shapeWidth = if (shapeCount > 1) rw / (shapeCount - 1) else rw
        val shapeHeight = rh / 3f
        //正中上方的shape
        shapeRect.set(
            (w / 2f - shapeWidth / 2f),
            paddingTop.toFloat(),
            (w / 2f + shapeWidth / 2f),
            paddingTop.toFloat() + shapeHeight
        )
        round = shapeWidth / 2f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        for (i in 0 until shapeCount) {
            paint.color = shapeColor[getColorIndex(i + pos)]
            canvas.drawRoundRect(shapeRect, round, round, paint)
            canvas.rotate(degrees, center.x, center.y)
        }

        pos++
        if (pos >= shapeCount) {
            pos = 0
        }

        if (!isInEditMode) {
            postInvalidateDelayed(speed)
        }
    }

    private fun getColorIndex(i: Int) = when {
        i < 0 -> i + shapeCount
        i >= shapeCount -> i - shapeCount
        else -> i
    }
}