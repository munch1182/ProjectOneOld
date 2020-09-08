package com.munch.test.view.weight

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.munch.lib.libnative.helper.ResHelper
import com.munch.test.R

/**
 * Create by munch on 2020/9/7 21:49
 */
class RulerView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var target = 0.0f

    //刻度线间隔
    var lineWidth: Float

    //短刻度线高度
    var lineHeight: Float
    var strokeWidth: Float

    var min: Float
    var max: Float
    private var moveDistance: Float = 0f
    private var fontMetrics: Paint.FontMetrics
    var colorDef = Color.parseColor("#dedade")
    var colorCenter = ResHelper.getColor(resId = R.color.colorPrimary)
    private val countArray = ArrayList<Float>()

    private var startX = 0f

    init {
        lineWidth = ResHelper.dp2Px(dpVal = 15f)
        strokeWidth = ResHelper.dp2Px(dpVal = 5f)
        lineHeight = lineWidth * 2
        min = 0.0f
        max = 100.0f
        target = 0.0f

        paint.style = Paint.Style.FILL
        fontMetrics = paint.fontMetrics
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec)*/
        setMeasuredDimension(
            (MeasureSpec.getSize(widthMeasureSpec)),
            (lineHeight * 5 / 2).toInt() + paddingTop + paddingBottom
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.strokeWidth = strokeWidth / 2
        paint.color = colorDef
        paint.strokeCap = Paint.Cap.SQUARE
        val centerArray = floatArrayOf(measuredWidth / 2f, 0f, measuredWidth / 2f, lineHeight)
        val lineArray = floatArrayOf(
            0f, 0f, measuredWidth.toFloat(), 0f,
            0f, measuredHeight.toFloat(), measuredWidth.toFloat(), measuredHeight.toFloat()
        )
        paint.strokeWidth = strokeWidth / 2
        paint.color = colorDef
        canvas?.drawLines(lineArray, paint)
        countArray.clear()
        val count = (measuredWidth / lineWidth).toInt()
        countArray.addAll(centerArray.toList())
        for (i in 0..count / 2) {
            val leftX = centerArray[0] + i * lineWidth + moveDistance
            val rightX = centerArray[0] - i * lineWidth + moveDistance
            val lineHeight1 = if ((target + i) * 10 % 10 == 0f) {
                this.lineHeight
            } else {
                this.lineHeight / 2
            }
            val lineHeight2 = if ((target - i) * 10 % 10 == 0f) {
                this.lineHeight
            } else {
                this.lineHeight / 2
            }
            countArray.add(leftX)
            countArray.add(0f)
            countArray.add(leftX)
            countArray.add(lineHeight1)

            countArray.add(rightX)
            countArray.add(0f)
            countArray.add(rightX)
            countArray.add(lineHeight2)
        }
        canvas?.drawLines(countArray.toFloatArray(), paint)

        paint.color = colorCenter
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.ROUND
        canvas?.drawLines(centerArray, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                moveDistance = event.x - startX
                startX = event.x
                if (moveDistance > 0) {
                    target += moveDistance / lineWidth
                } else {
                    target -= moveDistance / lineWidth
                }
                invalidate()
            }
            else -> {
                startX = 0f
            }
        }
        return true
    }
}