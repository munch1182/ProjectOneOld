package com.munch.lib.weight.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.munch.lib.extend.paddingHorizontal
import com.munch.lib.log.log

/**
 * 绘制坐标轴
 */
class PointChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : BaseChartView(context, attrs, defStyleAttr, defStyleRes) {
    private val xCoordinate = CoordinateCalculateHelper()
    private val yCoordinate = CoordinateCalculateHelper()
    override val xPart: Part = XPart(xCoordinate)
    override val yPart: Part = YPart(yCoordinate)
    override val dataPart: Part = DataPart()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val dis = MeasureSpec.getSize(widthMeasureSpec).toFloat() - paddingHorizontal()
        xCoordinate.setAutoUnitDis(dis, 24 * 60 / 5)
        yCoordinate.setAutoLen(10, 15f)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        set(MutableList(xCoordinate.unitCount) {
            /*Random.nextInt(yCoordinate.unitCount - 1).toFloat()*/
            3f
        })
    }

    fun set(list: MutableList<Float>) {
        (dataPart as DataPart).set(list)
    }

    private class XPart(private val calculateHelper: CoordinateCalculateHelper) : Part {
        override val measureW: Float
            get() = calculateHelper.distance
        override val measureH: Float
            get() = 45f
        private val paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

        override fun draw(canvas: Canvas, rectF: RectF) {
            paint.color = Color.BLUE
            calculateHelper.setAutoUnitDis(rectF.width(), calculateHelper.unitCount)
            canvas.drawLine(rectF.left, rectF.top, rectF.right, rectF.top, paint)

            for (i in 1..calculateHelper.unitCount) {
                val x = rectF.left + i * calculateHelper.unitDis
                canvas.drawLine(x, rectF.top, x, rectF.top - 15f, paint)
            }
        }
    }

    private class YPart(private val calculateHelper: CoordinateCalculateHelper) : Part {
        override val measureW: Float
            get() = 45f
        override val measureH: Float
            get() = calculateHelper.distance
        private val paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

        override fun draw(canvas: Canvas, rectF: RectF) {
            paint.color = Color.RED
            calculateHelper.setAutoUnitDis(rectF.height(), calculateHelper.unitCount)
            canvas.drawLine(rectF.right, rectF.top, rectF.right, rectF.bottom, paint)

            for (i in 1..calculateHelper.unitCount) {
                val y = rectF.bottom - i * calculateHelper.unitDis
                canvas.drawLine(rectF.right, y, rectF.right + 15f, y, paint)
            }
        }
    }

    private inner class DataPart() : Part {
        override val measureW: Float
            get() = xPart.measureW
        override val measureH: Float
            get() = yPart.measureH
        private val paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.GREEN } }
        private var yList = mutableListOf<Float>()

        override fun draw(canvas: Canvas, rectF: RectF) {
            val lines = convertPoint(rectF)
            lines.forEachIndexed { index, fl ->
                log(index, fl)
            }
            canvas.drawLines(lines, paint)
        }

        fun set(list: MutableList<Float>) {
            this.yList = list
        }

        private fun convertPoint(rectF: RectF): FloatArray {
            return FloatArray(yList.size * 4) {
                val ri = it / 4
                val i = when {
                    it % 4 == 0 || it % 4 == 2 -> rectF.left + ri * xCoordinate.unitDis
                    it % 4 == 1 || it % 4 == 3 -> rectF.bottom - yList[ri] * yCoordinate.unitDis
                    else -> throw IllegalStateException()
                }
                i
            }
        }

    }

}