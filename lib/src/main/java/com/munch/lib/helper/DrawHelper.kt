package com.munch.lib.helper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import androidx.annotation.FloatRange
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt


/**
 * Create by munch1182 on 2021/1/15 15:11.
 */
object PosHelper {

    /**
     * 获取二阶贝塞尔曲线的点
     * x、y轴应该分别求值
     *
     * @param t 运行的阶段
     * @param v1 起始点
     * @param v2 控制点
     * @param v3 结束点
     */
    fun getPos(@FloatRange(from = 0.0, to = 1.0) t: Float, v1: Float, v2: Float, v3: Float): Float {
        //根据二阶贝塞尔公式
        // B(t) = (1-t)^2*P0 + 2*t*(1-t)*P1+t^2*P2
        return (1f - t).pow(2) * v1 + 2f * t * (1f - t) * v2 + t.pow(2) * v3
    }

    /**
     * 获取同一直线上两个点的距离
     * 不支持负数
     */
    fun getDis(v1: Float, v2: Float) = (v1.absoluteValue - v2.absoluteValue).absoluteValue

    /**
     * 获取两个点中点的坐标，无需判断两个点的方向
     * 不支持负数
     */
    fun getCenterPos(v1: Float, v2: Float): Float {
        return when {
            v1 == v2 -> return v1
            v1 < v2 -> v1
            else -> v2
        } + getDis(v1, v2) / 2f
    }

    /**
     * 根据勾股定理获取两个点之间的距离
     */
    fun getDis(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        return sqrt((getDis(x1, x2).pow(2f) + getDis(y1, y2).pow(2f)).toDouble())
    }
}

/**
 * 将cx,xy作为中心点绘制文字
 */
fun Canvas.drawTextInCenter(text: String, cx: Float, cy: Float, paint: Paint) {
    val textWidth = paint.measureText(text)
    val fontMetrics = paint.fontMetrics
    val baseLineY = cy + (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
    drawText(text, cx - textWidth / 2f, baseLineY, paint)
}

/**
 * 给定宽高等比放大bitmap
 *
 * 注意：origin未被回收
 */
fun Bitmap.scaleBitmap(newWidth: Int, newHeight: Int): Bitmap {
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight) // 使用后乘
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
}


/**
 * 按比例缩放图片
 *
 * 注意：origin未被回收
 */
fun Bitmap.scaleBitmap(ratio: Float): Bitmap? {
    val matrix = Matrix()
    matrix.preScale(ratio, ratio)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
}