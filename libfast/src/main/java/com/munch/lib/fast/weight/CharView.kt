package com.munch.lib.fast.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.munch.pre.lib.helper.measureMaxTextSpace

/**
 * Create by zhoumx on 2021/7/21 19:29.
 */
class CharView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    //配置
    private var builder = Builder()

    //原点
    private val center = PointF()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val xItems = arrayOf<String>()
    val yItems = arrayOf<String>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        paint.textSize = builder.textSizePx
        //测量最大文字的宽高
        val textSpace = paint.measureMaxTextSpace(xItems)
        val x = textSpace.first + builder.textXPaddingPx
        val y = textSpace.second + builder.textYPaddingPx
        center.set(x, y)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        //绘制背景
        drawBg(canvas)
        //绘制坐标系线
        drawCoordinate(canvas)
        //绘制坐标单位
        drawUnit(canvas)
        //绘制数据
        drawData(canvas)
        //绘制选择
        drawSelect(canvas)
    }

    private fun drawSelect(canvas: Canvas) {
    }

    private fun drawData(canvas: Canvas) {
    }

    private fun drawUnit(canvas: Canvas) {
    }

    private fun drawCoordinate(canvas: Canvas) {
    }

    private fun drawBg(canvas: Canvas) {
    }

    open class Builder {

        //坐标线高度
        var coordinateLineHeight = 1f

        //坐标线颜色
        var coordinateLineColor = Color.parseColor("#999999")

        //坐标轴文字大小
        var textSizePx = 24f

        //x轴坐标轴文字间隔
        var textXPaddingPx = 5f

        //y轴坐标轴文字间隔
        var textYPaddingPx = 5f

        //x轴坐标轴剩余长度，即超过item故意留下的长度，该长度不参与item间距的分配
        var xCoordinateLineLeftPx = 15f

        //y轴坐标轴剩余长度，即超过item故意留下的长度，该长度不参与item间距的分配
        var yCoordinateLineLeftPx = 15f

        open fun build(view: CharView) {
            /*view.builder.compare(this, view)*/
            view.builder = this
        }

        //比较两个数据，用以判断是否需要重新测量
        //不需要测量
        /*protected open fun compare(builder: Builder, view: CharView) {
            if (this == builder) {
                return
            }
            if (builder.coordinateLineHeight != coordinateLineHeight
                || builder.textSizePx != textSizePx
                || builder.textXPaddingPx != textXPaddingPx
                || builder.textYPaddingPx != textYPaddingPx
            ) {
                view.requestLayout()
            }
            view.invalidate()
        }*/

        override fun hashCode(): Int {
            return floatArrayOf(
                coordinateLineHeight,
                coordinateLineColor.toFloat(),
                textSizePx,
                textXPaddingPx,
                textYPaddingPx
            ).contentHashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Builder
            if (coordinateLineHeight != other.coordinateLineHeight) return false
            if (coordinateLineColor != other.coordinateLineColor) return false
            if (textSizePx != other.textSizePx) return false
            if (textXPaddingPx != other.textXPaddingPx) return false
            if (textYPaddingPx != other.textYPaddingPx) return false
            return true
        }
    }
}