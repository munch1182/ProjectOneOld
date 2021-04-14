package com.munch.lib.fast.weight

import android.graphics.*
import android.graphics.drawable.Drawable
import com.munch.pre.lib.helper.lineTo
import com.munch.pre.lib.helper.moveTo
import com.munch.pre.lib.helper.quadTo
import kotlin.math.cos
import kotlin.math.sin

/**
 * Create by munch1182 on 2021/2/2 10:49.
 */
class FishDrawableWithStructure : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#99ffb6c1")
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
    }
    private val path = Path()
    private var bodyRate = 1.2f

    //鱼头半径
    private var headRadius = HEAD_RADIUS_DEF
    private var sideLength = /*8.38f*/20f * headRadius
    private var bodyLength = 1.6f * headRadius
    private var finsLength = bodyLength / 1.5f

    private var drawStructure = false

    fun drawStructure(draw: Boolean = false) {
        this.drawStructure = draw
        invalidateSelf()
    }

    companion object {
        private const val HEAD_RADIUS_DEF = 285f
    }

    override fun draw(canvas: Canvas) {
        /*if (drawStructure) {
            canvas.drawColor(Color.YELLOW)
        }*/
        canvas.drawColor(Color.WHITE)
        //中心点，即鱼肚中心，也是坐标系的原点
        val centerPoint = PointF(intrinsicWidth / 2f, intrinsicHeight / 2f)
        //根据鱼肚长得出鱼头中心点
        val headPoint = calculatePoint(centerPoint, bodyLength, 0f)
        canvas.drawCircle(headPoint.x, headPoint.y, headRadius, paint)

        val endPoint = calculatePoint(centerPoint, bodyLength, 180f)
        val headLeftPoint = calculatePoint(headPoint, headRadius, 80f)
        val headRightPoint = calculatePoint(headPoint, headRadius, -80f)
        val bodyLeftPoint = calculatePoint(endPoint, 0.7f * headRadius, 90f)
        val bodyRightPoint = calculatePoint(endPoint, 0.7f * headRadius, -90f)

        val controlLeft = calculatePoint(headPoint, bodyLength * bodyRate, 130f)
        val controlRight = calculatePoint(headPoint, bodyLength * bodyRate, -130f)

        //左侧鱼鳍的开始点
        val finLeftStart = calculatePoint(headPoint, 0.9f * headRadius, 115f)
        val finLeftEnd = calculatePoint(finLeftStart, finsLength, 180f)
        val finLeftControl = calculatePoint(finLeftStart, 1.8f * finsLength, 110f)

        //右侧鱼鳍的开始点
        val finRightStart = calculatePoint(headPoint, 0.9f * headRadius, -115f)
        val finRightEnd = calculatePoint(finRightStart, finsLength, 180f)
        val finRightControl = calculatePoint(finRightStart, 1.8f * finsLength, -110f)

        //鱼尾部分1
        val tailEndPointLeft = calculatePoint(endPoint, 0.7f * headRadius, 90f)
        val tailEndPointRight = calculatePoint(endPoint, 0.7f * headRadius, -90f)
        val end2Point = calculatePoint(endPoint, 0.9f * headRadius, 180f)
        val tailLengthRate = 0.45f
        val tailEnd2PointLeft = calculatePoint(end2Point, tailLengthRate * headRadius, 90f)
        val tailEnd2PointRight = calculatePoint(end2Point, tailLengthRate * headRadius, -90f)
        //鱼尾部分2
        val tailEnd2Point2Left = calculatePoint(end2Point, tailLengthRate * headRadius, 90f)
        val tailEnd2Point2Right = calculatePoint(end2Point, tailLengthRate * headRadius, -90f)
        val end3Point = calculatePoint(end2Point, 1.2f * headRadius, 180f)
        val tailEnd3PointLeft = calculatePoint(end3Point, 0.2f * headRadius, 90f)
        val tailEnd3PointRight = calculatePoint(end3Point, 0.2f * headRadius, -90f)
        //鱼尾部分3
        val tail2Left = calculatePoint(end2Point, 0.9f * headRadius, 145f)
        val tail2Right = calculatePoint(end2Point, 0.9f * headRadius, -145f)
        val tail2Left2 = calculatePoint(end2Point, 1.2f * headRadius, 140f)
        val tail2Right2 = calculatePoint(end2Point, 1.2f * headRadius, -140f)

        path.reset()
        path.quadTo(headLeftPoint, controlLeft, bodyLeftPoint)
        path.lineTo(bodyRightPoint)
        path.quadTo(controlRight, headRightPoint)
        path.close()
        canvas.drawPath(path, paint)

        path.reset()
        path.quadTo(finLeftStart, finLeftControl, finLeftEnd)
        path.close()
        canvas.drawPath(path, paint)

        path.reset()
        path.quadTo(finRightStart, finRightControl, finRightEnd)
        path.close()
        canvas.drawPath(path, paint)

        path.reset()
        path.moveTo(tailEndPointLeft)
        path.lineTo(tailEndPointRight, tailEnd2PointRight, tailEnd2PointLeft)
        path.close()
        canvas.drawPath(path, paint)

        path.reset()
        path.moveTo(tailEnd2Point2Left)
        path.lineTo(tailEnd2Point2Right, tailEnd3PointRight, tailEnd3PointLeft)
        path.close()
        canvas.drawPath(path, paint)

        canvas.drawCircle(endPoint.x, endPoint.y, 0.7f * headRadius, paint)
        canvas.drawCircle(end2Point.x, end2Point.y, tailLengthRate * headRadius, paint)
        canvas.drawCircle(end3Point.x, end3Point.y, 0.2f * headRadius, paint)

        path.reset()
        path.moveTo(end2Point)
        path.lineTo(tail2Left, tail2Right)
        path.close()
        canvas.drawPath(path, paint)

        path.reset()
        path.moveTo(end2Point)
        path.lineTo(tail2Left2, tail2Right2)
        path.close()
        canvas.drawPath(path, paint)
        if (drawStructure) {
            drawPot(
                canvas,
                centerPoint, headPoint, endPoint,
                headLeftPoint, headRightPoint, controlLeft, controlRight,
                finLeftStart, finLeftEnd, finLeftControl,
                finRightStart, finRightEnd, finRightControl,
                tailEndPointLeft, tailEndPointRight, tailEnd2PointLeft, tailEnd2PointRight,
                tailEnd2PointLeft, tailEnd2PointRight, tailEnd3PointRight, tailEnd3PointLeft
            )

        }
    }

    private fun drawPot(canvas: Canvas, vararg point: PointF) {
        paint.color = Color.RED
        point.forEach {
            canvas.drawCircle(it.x, it.y, 5f, paint)
        }
    }


    /**
     * 输入起点、长度、旋转角度计算终点
     */
    private fun calculatePoint(start: PointF, length: Float, angle: Float): PointF {
        val deltaX = (cos(Math.toRadians(angle.toDouble())) * length).toFloat()
        val deltaY = (sin(Math.toRadians((angle - 180f).toDouble())) * length).toFloat()
        return PointF(start.x + deltaX, start.y + deltaY)
    }

    override fun getIntrinsicHeight(): Int {
        return sideLength.toInt()
    }

    override fun getIntrinsicWidth(): Int {
        return sideLength.toInt()
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        //决定绘制的部分是否遮住Drawable下边的东西，有几种模式
        //PixelFormat.UNKNOWN
        //PixelFormat.TRANSLUCENT 只有绘制的地方才盖住下边
        //PixelFormat.TRANSPARENT 透明，不显示绘制内容
        //PixelFormat.OPAQUE 完全盖住下边内容
        return PixelFormat.TRANSLUCENT
    }
}