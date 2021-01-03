package com.munch.project.test.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.munch.lib.helper.dp2Px
import kotlin.math.ceil

/**
 * 仿直尺效果
 * 没有做成控件所以没有写attr
 *      做成控件可以考虑每刻度大小倍数、5格可选、按刻度移动等
 * 为了避免float的精度丢失问题，有些数据被扩大了10倍 {@see LineHelper.getNum}
 *
 *
 * Create by munch on 2020/9/7 21:49
 */
class RulerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    var target = 0.0f

    //刻度线间隔
    var lineWidth: Float

    //短刻度线高度
    var lineHeight: Float
    var strokeWidth: Float

    var min: Float
    var max: Float

    private var startX = 0f
    private var halfTextHeight = 0f
    private var moveDistance: Float = 0f
    private var fontMetrics: Paint.FontMetrics
    var colorDef = Color.parseColor("#dedade")
    var colorCenter = Color.parseColor("#ff03dac5")
    private var listener: UpdateListener? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var helper: LineHelper


    init {
        lineWidth = getContext().dp2Px(15f)
        strokeWidth = getContext().dp2Px(5f)
        lineHeight = lineWidth * 2
        min = 0.0f
        max = 100.0f
        target = 0.0f

        paint.style = Paint.Style.FILL
        paint.textSize = 50f
        fontMetrics = paint.fontMetrics
        halfTextHeight = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom

        helper = LineHelper(lineHeight)
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

        helper.reset()

        val count = (measuredWidth / lineWidth).toInt()
        //中心线
        helper.sX = centerArray[0] + moveDistance
        helper.num = target * 10f
        helper.update()

        for (i in 1..count / 2 + 1) {
            val leftX = centerArray[0] + i.toFloat() * lineWidth + moveDistance
            val rightX = centerArray[0] - i.toFloat() * lineWidth + moveDistance
            helper.sX = leftX
            helper.num = target * 10 + i.toFloat()
            if (helper.num <= max * 10) {
                helper.update()
            }

            helper.sX = rightX
            helper.num = target * 10 - i.toFloat()
            if (helper.num > min * 10) {
                helper.update()
            }
        }

        canvas?.drawLines(helper.linesArray.toFloatArray(), paint)

        helper.textArray.forEachIndexed { index, float10 ->
            //因为增大了10倍
            val strFinal = if (float10 != 0f) {
                float10.toString().subSequence(0, float10.toString().indexOf('.') - 1)
                    .toString()
            } else {
                "0"
            }
            val textWidth = paint.measureText(strFinal)
            canvas?.drawText(
                strFinal, helper.textXYArray[index * 2] - textWidth / 2,
                helper.textXYArray[index * 2 + 1] + halfTextHeight, paint
            )
        }

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
                //往右移动为负数
                val next = target - moveDistance / lineWidth
                when {
                    next < min -> {
                        target = min
                    }
                    next > max -> {
                        target = max
                    }
                    else -> {
                        //按刻度移动
                        target -= moveDistance / lineWidth
                    }
                }
                target = getCeilFloat(target)
                listener?.update(target)
                invalidate()
            }
            else -> {
                startX = 0f
            }
        }
        return true
    }

    private fun getCeilFloat(float: Float): Float {
        return (ceil((float * 10).toDouble()) * 0.1).toFloat()
    }

    fun setUpdateListener(listener: UpdateListener) {
        this.listener = listener
    }

    @FunctionalInterface
    interface UpdateListener {
        fun update(num: Float)
    }

    private class LineHelper(private val lineHeight: Float) {
        var sX: Float = 0f
            set(value) {
                eX = value
                field = value
            }
        var sY: Float = 0f
        var eX: Float = 0f
        var eY: Float = 0f
        var num: Float = 0f
            set(value) {
                field = value
                eY = getLineHeight()
            }
        private var isHeightLine = false
        var linesArray = ArrayList<Float>()

        //文字中心点
        var textXYArray = ArrayList<Float>()

        //该数组中的数增大了10倍
        var textArray = ArrayList<Float>()

        fun reset() {
            linesArray.clear()
            textXYArray.clear()
            textArray.clear()
        }

        /**
         * 完成一条线之后调用
         */
        fun update() {
            linesArray.add(sX)
            linesArray.add(sY)
            linesArray.add(eX)
            linesArray.add(eY)
            if (isHeightLine) {
                textXYArray.add(sX)
                textXYArray.add(lineHeight * 3 / 2)
                textArray.add(num)
            }
        }

        fun getLineHeight(): Float {
            //因为num增大了10倍
            isHeightLine = num.toInt() % 10 == 0
            return if (isHeightLine) {
                lineHeight
            } else {
                lineHeight / 2
            }
        }

        override fun toString(): String {
            return "sx：$sX,sY：$sY,eX：$eX,eY：$eY,num：$num"
        }
    }
}