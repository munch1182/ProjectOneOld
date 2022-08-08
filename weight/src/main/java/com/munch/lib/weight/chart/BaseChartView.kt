package com.munch.lib.weight.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.munch.lib.extend.paddingHorizontal
import com.munch.lib.extend.paddingVertical

/**
 * 图表结构, 分为x轴区域(x轴到底部的区域(不相交y轴))、y轴区域(y轴到左侧的区域(不相交x轴))及数据区域
 */
abstract class BaseChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    protected abstract val xPart: Part
    protected abstract val yPart: Part
    protected abstract val dataPart: Part
    protected open val xRect = RectF()
    protected open val yRect = RectF()
    protected open val dataRect = RectF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        xPart.onMeasure()
        yPart.onMeasure()
        dataPart.onMeasure()
        val h = (xPart.measureH + dataPart.measureH + paddingVertical()).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val rw = w - paddingHorizontal()
        val rh = h - paddingVertical()
        xRect.set(
            paddingLeft + yPart.measureW,
            h.toFloat() - paddingBottom - xPart.measureH,
            w.toFloat() - paddingEnd,
            h.toFloat() - paddingBottom
        )
        yRect.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            paddingLeft + yPart.measureW,
            h.toFloat() - paddingBottom - xPart.measureH,
        )
        dataRect.set(xRect.left, yRect.top, xRect.right, yRect.bottom)
        xPart.onSizeChange(this, rw, rh)
        yPart.onSizeChange(this, rw, rh)
        dataPart.onSizeChange(this, rw, rh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        xPart.draw(canvas, xRect)
        yPart.draw(canvas, yRect)
        dataPart.draw(canvas, dataRect)
    }

    // 区域划分
    protected interface Part {

        val measureW: Float
        val measureH: Float

        fun onMeasure() {}

        fun onSizeChange(view: BaseChartView, rw: Int, rh: Int) {}

        fun draw(canvas: Canvas, rectF: RectF)
    }

    // 计算坐标
    protected class CoordinateCalculateHelper {

        //单位间距长度
        var unitDis: Float = 10f
            private set

        //要显示的间距数量(未必绘制)
        var unitCount: Int = 10
            private set

        //长度 
        var distance: Float = unitDis * unitCount
            private set

        fun setAutoLen(unitCount: Int, unitDis: Float) {
            this.unitDis = unitDis
            this.unitCount = unitCount
            distance = unitDis * unitCount
        }

        fun setAutoCount(distance: Float, unitDis: Float) {
            this.unitDis = unitDis
            this.distance = distance
            unitCount = (distance / unitDis).toInt()
        }

        fun setAutoUnitDis(distance: Float, unitCount: Int) {
            this.unitCount = unitCount
            this.distance = distance
            unitDis = distance / unitCount
        }

        override fun toString(): String {
            return "Coordinate(unitDis=$unitDis, unitCount=$unitCount, distance=$distance)"
        }


    }
}