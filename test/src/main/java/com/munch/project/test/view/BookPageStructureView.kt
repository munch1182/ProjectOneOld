package com.munch.project.test.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import com.munch.lib.Point
import com.munch.lib.helper.RectArrayHelper
import kotlin.math.absoluteValue
import kotlin.math.pow

/**
 * Create by munch1182 on 2021/1/9 15:02.
 */
class BookPageStructureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        textSize = 40f
    }
    private val pointStart = Point()
    private val pointClick = Point()
    private val pathContent = Path()
    private val pointCache = Point()
    private var w = 0f
    private var h = 0f
    private val dashPathEffect by lazy { DashPathEffect(floatArrayOf(10f, 5f), 0f) }
    private val xfermodeATop by lazy { PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP) }
    private val xfermodeClear by lazy { PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private val rectHelper = RectArrayHelper()
    private lateinit var bitmapHolder: Bitmap
    private lateinit var bitmapCanvas: Canvas
    private var drawStructure = true
    private var minMoveDis2Next = 200f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        if (pointStart.isSet()) {
            //清空画布
            clearCanvas()
            //点击点所在区域的顶点
            val pointEnd = getEndPointInArea(pointClick)
            //四角
            if (pointEnd != null) {
                limitPointEnd(pointEnd)
                drawFlipPathFromAngle(bitmapCanvas, pointEnd)
                //左右
            } else {
                pointCache.reset(pointStart)
                limitPointEnd(pointCache)
                drawFlipPathFromLR(bitmapCanvas, pointCache)
            }
        }
        if (drawStructure) {
            drawArea(bitmapCanvas)
        }
        canvas.drawBitmap(bitmapHolder, 0f, 0f, paint)
    }

    private fun clearCanvas() {
        paint.xfermode = xfermodeClear
        bitmapCanvas.drawPaint(paint)
        paint.xfermode = null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        measureArea()
        bitmapHolder = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmapHolder)
    }

    companion object {

        const val AREA_B_R = 0
        const val AREA_R = 1
        const val AREA_T_R = 2
        const val AREA_L = 3
        const val AREA_C = 4
        const val AREA_T_L = 5
        const val AREA_B_L = 6
    }

    private fun measureArea() {
        w = width / 3f
        h = height / 3f
        //此处顺序必须与[AREA]的值的大小一致
        rectHelper.setArray(
            arrayListOf(
                //l、t、r、b
                width.toFloat() / 2f, h * 2f, width.toFloat(), height.toFloat(),//bottom_right
                w * 2f, h, width.toFloat(), h * 2f,//right
                width.toFloat() / 2f, 0f, width.toFloat(), h,//top_right
                0f, h, w, h * 2f,//left
                w, h, w * 2f, h * 2f,//center
                0f, 0f, width.toFloat() / 2f, h,//top_left
                0f, h * 2f, width.toFloat() / 2f, height.toFloat(),//bottom_left
            )
        )
    }

    /**
     * 获取[pointStart]所在区域的对应顶点
     * left是左下点
     * bottom是右下点
     * top是右上点
     * right、center返回为null
     */
    private fun getEndPointInArea(point: Point): Point? {
        rectHelper.run {
            val ratioH = point.y / h
            val disW = width.toFloat() / 2f
            //top
            if (ratioH < 1f) {
                //top_left
                if (point.x < disW) {
                    pointCache.reset(getLeft(AREA_T_L), getTop(AREA_T_L))
                    //top_right
                } else {
                    pointCache.reset(getRight(AREA_T_R), getTop(AREA_T_R))
                }
            }
            //bottom
            else if (ratioH > 2f) {
                //bottom_left
                if (point.x < disW) {
                    pointCache.reset(getLeft(AREA_B_L), getBottom(AREA_B_L))
                    //bottom_right
                } else {
                    pointCache.reset(getRight(AREA_B_R), getBottom(AREA_B_R))
                }
            } else {
                return null
            }

        }
        return pointCache
    }

    fun drawStructure(draw: Boolean = !this.drawStructure) {
        if (this.drawStructure != draw) {
            this.drawStructure = draw
            invalidate()
        }
    }

    private fun drawArea(canvas: Canvas) {
        if (!drawStructure) {
            return
        }
        paint.color = Color.GRAY
        paint.pathEffect = dashPathEffect
        paint.strokeWidth = 1f
        rectHelper.run {
            canvas.drawLines(
                floatArrayOf(
                    //top-left右边线
                    getRight(AREA_T_L), getTop(AREA_T_L),
                    getRight(AREA_T_L), getBottom(AREA_T_L),
                    //left到right上边线
                    getLeft(AREA_L), getTop(AREA_L),
                    getRight(AREA_R), getTop(AREA_R),
                    //left到right下边线
                    getLeft(AREA_L), getBottom(AREA_L),
                    getRight(AREA_R), getBottom(AREA_R),
                    //bottom-left右边线
                    getRight(AREA_B_L), getTop(AREA_B_L),
                    getRight(AREA_B_L), getBottom(AREA_B_L),
                    //center左边线
                    getLeft(AREA_C), getTop(AREA_C),
                    getLeft(AREA_C), getBottom(AREA_C),
                    //center右边线
                    getRight(AREA_C), getTop(AREA_C),
                    getRight(AREA_C), getBottom(AREA_C)
                ), paint
            )

            drawText(canvas, "left", getCenterX(AREA_L), getCenterY(AREA_L), paint)
            drawText(canvas, "top-right", getCenterX(AREA_T_R), getCenterY(AREA_T_R), paint)
            drawText(canvas, "top-left", getCenterX(AREA_T_L), getCenterY(AREA_T_L), paint)
            drawText(canvas, "center", getCenterX(AREA_C), getCenterY(AREA_C), paint)
            drawText(canvas, "right", getCenterX(AREA_R), getCenterY(AREA_R), paint)
            drawText(canvas, "bottom-right", getCenterX(AREA_B_R), getCenterY(AREA_B_R), paint)
            drawText(canvas, "bottom-left", getCenterX(AREA_B_L), getCenterY(AREA_B_L), paint)
        }
    }

    private fun drawText(canvas: Canvas, text: String, cx: Float, cy: Float, paint: Paint) {
        val textWidth = paint.measureText(text)
        val fontMetrics = paint.fontMetrics
        val baseLineY = cy + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        canvas.drawText(text, cx - textWidth / 2, baseLineY, paint)
    }

    private fun drawFlipPathFromLR(canvas: Canvas, clickPoint: Point) {
        val isLeft = pointClick.x < w
        val aX = if (isLeft) 0f else clickPoint.x
        val cX = if (isLeft) clickPoint.x else width.toFloat()
        val bX = getCenterVal(aX, cX)
        val topY = 0f
        val bottomY = height.toFloat()
        pathContent.reset()
        pathContent.moveTo(aX, topY)
        pathContent.lineTo(bX, topY)
        pathContent.lineTo(bX, bottomY)
        pathContent.lineTo(aX, bottomY)
        pathContent.close()
        canvas.drawPath(pathContent, if (isLeft) pageNextPaint() else pageBackPaint())

        pathContent.reset()
        pathContent.moveTo(bX, topY)
        pathContent.lineTo(cX, topY)
        pathContent.lineTo(cX, bottomY)
        pathContent.lineTo(bX, bottomY)
        pathContent.close()
        canvas.drawPath(pathContent, if (isLeft) pageBackPaint() else pageNextPaint())

        if (!drawStructure) {
            return
        }
        canvas.drawLines(floatArrayOf(aX, topY, bX, topY, bX, topY, aX, topY), lineHelperPaint())
        drawPosText(canvas, "a", aX, topY)
        drawPosText(canvas, "b", bX, topY)
        drawPosText(canvas, "c", cX, topY)
    }

    private fun drawFlipPathFromAngle(canvas: Canvas, pointEnd: Point) {

        //<editor-fold desc="计算结构点">
        //点击点a
        val aX = pointStart.x
        val aY = pointStart.y
        //顶点b
        val bX = pointEnd.x
        val bY = pointEnd.y

        //点击点a与顶点b的中心点c
        val c1X = getCenterVal(aX, bX)
        val c1Y = getCenterVal(aY, bY)
        //中心c在水平方向与边的垂直线
        val c11X = c1X
        val c11Y = bY

        //根据中线点获取中垂线与边的交点
        val deArray = getCenterLine(c1X, c1Y, bX, bY)
        val d1X = deArray[0]
        val d1Y = deArray[1]
        val e1X = deArray[2]
        val e1Y = deArray[3]

        //同理
        val c2X = getCenterVal(aX, c1X)
        val c2Y = getCenterVal(aY, c1Y)

        val de2Array = getCenterLine(c2X, c2Y, bX, bY)
        val d2X = de2Array[0]
        val d2Y = de2Array[1]
        val e2X = de2Array[2]
        val e2Y = de2Array[3]

        //a-d1与d2-e2的交点f

        // y =  k * x + a
        // y1 = k * x1 + a
        // y2 = k * x2 + a
        // k = (y1-y2)/(x1-x2)
        // a = y1 - k*x1)
        val kAD1 = (aY - d1Y) / (aX - d1X)
        val aAD1 = aY - kAD1 * aX
        //a-d1 : yAD1 = kAD1 * xAD1 + aAD1
        val kD2E2 = (d2Y - e2Y) / (d2X - e2X)
        val aD2E2 = d2Y - kD2E2 * d2X
        //d2-e2 : yD2E2 = kD2E2 * xD2E2 + aD2E2

        //yD2E2 = yAD1 xD2E2 = xAD1 = fX
        //kAD1 * xAD1 + aAD1  = kD2E2 * xD2E2 + aD2E2
        //kAD1 * fX - kD2E2 * fx = aD2E2 - aAD1
        // fx = (aD2E2 - aAD1)/(kAD1-kD2E2)
        // fy = kAD1 * fx + aAD1
        val f1X = (aD2E2 - aAD1) / (kAD1 - kD2E2)
        val f1Y = kAD1 * f1X + aAD1

        //a-e1 d2-e2
        val kAE1 = (aY - e1Y) / (aX - e1X)
        val aAE1 = aY - kAE1 * aX
        val f2X = (aD2E2 - aAE1) / (kAE1 - kD2E2)
        val f2Y = kAE1 * f2X + aAE1

        //根据二阶贝塞尔公式获取中点g
        // B(t) = (1-t)^2*P0 + 2*t*(1-t)*P1+t^2*P2
        val t = 0.5f
        val g2X = (1f - t).pow(2) * e2X + 2f * t * (1f - t) * e1X + t.pow(2) * f2X
        val g2Y = (1f - t).pow(2) * e2Y + 2f * t * (1f - t) * e1Y + t.pow(2) * f2Y

        val g1X = (1f - t).pow(2) * f1X + 2f * t * (1f - t) * d1X + t.pow(2) * d2X
        val g1Y = (1f - t).pow(2) * f1Y + 2f * t * (1f - t) * d1Y + t.pow(2) * d2Y
        //</editor-fold>

        //<editor-fold desc="绘制背景">
        /*canvas.drawColor(Color.GREEN)*/
        //</editor-fold>

        //<editor-fold desc="绘制翻页效果区域">
        pathContent.reset()
        pathContent.lineTo(e2X, e2Y)
        pathContent.quadTo(e1X, e1Y, f2X, f2Y)
        pathContent.lineTo(aX, aY)
        pathContent.lineTo(f1X, f1Y)
        pathContent.quadTo(d1X, d1Y, d2X, d2Y)
        pathContent.lineTo(bX, bY)
        pathContent.lineTo(e2X, e2Y)
        pathContent.close()
        canvas.drawPath(pathContent, pageNextPaint())
        //</editor-fold>

        //<editor-fold desc="绘制翻页背面效果">
        pathContent.reset()
        pathContent.moveTo(aX, aY)
        pathContent.lineTo(f1X, f1Y)
        pathContent.lineTo(g1X, g1Y)
        pathContent.lineTo(g2X, g2Y)
        pathContent.lineTo(f2X, f2Y)
        pathContent.close()

        /*setLayerType(LAYER_TYPE_SOFTWARE, null)*/
        paint.xfermode = xfermodeATop
        canvas.drawPath(pathContent, pageBackPaint())
        paint.xfermode = null
        //</editor-fold>

        if (!drawStructure) {
            return
        }

        //<editor-fold desc="绘制结构">
        canvas.drawLine(aX, aY, bX, bY, lineHelperPaint())
        drawPosText(canvas, "a", aX, aY)
        drawPosText(canvas, "b", bX, bY)

        canvas.drawLine(c1X, c1Y, c11X, c11Y, lineHelperPaint())
        drawPosText(canvas, "c1", c1X, c1Y)
        drawPosText(canvas, "c11", c11X, c11Y)

        canvas.drawLine(d1X, d1Y, e1X, e1Y, linePaint())
        drawPosText(canvas, "d1", d1X, d1Y)
        drawPosText(canvas, "e1", e1X, e1Y)

        canvas.drawLine(d2X, d2Y, e2X, e2Y, linePaint())
        drawPosText(canvas, "d2", d2X, d2Y)
        drawPosText(canvas, "e2", e2X, e2Y)

        canvas.drawLine(aX, aY, d1X, d1Y, lineHelperPaint())

        drawPosText(canvas, "f1", f1X, f1Y)
        drawPosText(canvas, "f2", f2X, f2Y)
        drawPosText(canvas, "g1", g1X, g1Y)
        drawPosText(canvas, "g2", g2X, g2Y)
        canvas.drawLine(g1X, g1Y, g2X, g2Y, linePaint())
        //</editor-fold>
    }

    /**
     * @param cX 中线点坐标
     * @param bX 顶点坐标
     */
    private fun getCenterLine(
        cX: Float,
        cY: Float,
        bX: Float,
        bY: Float
    ): FloatArray {
        val c11X = cX
        val c11Y = bY
        val c12X = bX
        val c12Y = cY
        //通过欧几里德定理获取c1-d长度
        val disC1ToB = getDis(cY, c11Y).pow(2) / getDis(c11X, bX)
        val dX = getPosByDis(c11X, disC1ToB, bX)
        val dY = bY

        //同理获取c2-e的长度
        val disC2ToE = getDis(cX, c12X).pow(2) / getDis(c12Y, bY)
        val eX = bX
        val eY = getPosByDis(c12Y, disC2ToE, bY)

        return floatArrayOf(dX, dY, eX, eY)
    }

    private fun getPosByDis(pos: Float, dis: Float, pos2: Float): Float {
        return if (pos > pos2) pos + dis else pos - dis
    }

    private fun getDis(x1: Float, x2: Float): Float {
        return (x1.absoluteValue - x2.absoluteValue).absoluteValue
    }

    private fun getCenterVal(v1: Float, v2: Float): Float {
        return if (v1 < v2) (v2 - v1) / 2f + v1 else v2 + (v1 - v2) / 2f
    }

    private fun drawPosText(canvas: Canvas, text: String, cx: Float, cy: Float) {
        val posPaint = posPaint()
        val strWidth = posPaint.measureText(text)
        canvas.drawText(
            text,
            if (cx == width.toFloat()) cx - strWidth else cx,
            if (cy == 0f) cy + 30f else cy,
            posPaint
        )
    }

    /**
     * 传入两个点，获取垂直平分线与相应两边的交点
     */
    /*private fun getCenterXY(sX: Float, sY: Float, eX: Float, eY: Float): FloatArray {
        val cX = sX + (eX - sX) / 2f
        val cY = eY + (eY - sY) / 2f

    }*/

    private fun posDis(c1: Float, c2: Float) = (c1.absoluteValue - c2.absoluteValue).absoluteValue

    //<editor-fold desc="改变画笔">
    private fun posPaint(): Paint {
        paint.color = Color.RED
        paint.strokeWidth = 3f
        paint.pathEffect = null
        return paint
    }

    private fun linePaint(): Paint {
        paint.color = Color.MAGENTA
        paint.strokeWidth = 3f
        paint.pathEffect = dashPathEffect
        return paint
    }

    private fun textPaint(): Paint {
        paint.color = Color.RED
        paint.strokeWidth = 3f
        paint.pathEffect = null
        return paint
    }

    private fun pageBackPaint(): Paint {
        paint.pathEffect = null
        paint.color = Color.YELLOW
        return paint
    }

    private fun pageNextPaint(): Paint {
        paint.color = Color.BLUE
        paint.pathEffect = null
        return paint
    }

    private fun lineHelperPaint(): Paint {
        paint.color = Color.GRAY
        paint.strokeWidth = 3f
        paint.pathEffect = dashPathEffect
        return paint
    }
    //</editor-fold>

    //限制效果，防止第一次点击时过远显示效果夸张
    private fun limitPointEnd(pointEnd: Point) {

    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    //点击居中center
    private fun clickCenter() = pointClick.x in w..(2f * w) && pointClick.y in h..(2f * h)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                pointClick.reset(event.x, event.y)
                if (clickCenter()) {
                    pointStart.reset()
                    performClick()
                    //不重绘，交由外部处理
                    return false
                } else {
                    pointStart.reset(pointClick)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                pointStart.reset(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                if (isNextPage(event)) {
                    nextPageAnim()
                } else {
                    keepNowAnim()
                }
                pointClick.reset()
                pointStart.reset()
            }
        }
        invalidate()
        return true
    }

    private fun keepNowAnim() {

    }

    private fun nextPageAnim() {

    }

    /**
     * 根据滑动距离判断是否翻页
     */
    private fun isNextPage(event: MotionEvent): Boolean {
        return event.x - pointClick.x > minMoveDis2Next
    }

    @IntDef(AREA_B_R, AREA_R, AREA_T_R, AREA_L, AREA_C)
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Area
}