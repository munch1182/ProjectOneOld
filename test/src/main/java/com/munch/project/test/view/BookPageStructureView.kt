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
    private val pathFlip = Path()
    private val pathContent = Path()
    private val pointCache = Point()
    private var w = 0f
    private var h = 0f
    private val dashPathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
    private val rectHelper = RectArrayHelper()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        drawArea(canvas)
        drawFlipPath(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        measureArea()
    }

    companion object {

        const val AREA_B = 0
        const val AREA_R = 1
        const val AREA_T = 2
        const val AREA_L = 3
        const val AREA_C = 4
    }

    private fun measureArea() {
        w = width / 3f
        h = height / 3f
        //此处顺序必须与[AREA]的值的大小一致
        rectHelper.setArray(
            arrayListOf(
                //l、t、r、b
                w, h * 2f, width.toFloat(), height.toFloat(),//bottom
                w * 2f, h, width.toFloat(), h * 2f,//right
                w, 0f, width.toFloat(), h,//top
                0f, 0f, w, height.toFloat(),//left
                w, h, w * 2f, h * 2f,//center
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
            when {
                //left
                point.x in 0f..w -> {
                    pointCache.reset(getLeft(AREA_L), getBottom(AREA_L))
                }
                //top
                point.y in 0f..h -> {
                    pointCache.reset(getRight(AREA_T), getTop(AREA_T))
                }
                //bottom
                point.y in 2f * h..height.toFloat() -> {
                    pointCache.reset(getRight(AREA_B), getBottom(AREA_B))
                }
                //center、right
                else -> {
                    return null
                }
            }
        }
        return pointCache
    }

    private fun drawArea(canvas: Canvas) {
        paint.color = Color.GRAY
        paint.pathEffect = dashPathEffect
        rectHelper.run {
            canvas.drawLines(
                floatArrayOf(
                    //left右边线
                    getRight(AREA_L), getTop(AREA_L),
                    getRight(AREA_L), getBottom(AREA_L),
                    //top下边线
                    getLeft(AREA_T), getBottom(AREA_T),
                    getRight(AREA_T), getBottom(AREA_T),
                    //bottom上边线
                    getLeft(AREA_B), getTop(AREA_B),
                    getRight(AREA_B), getTop(AREA_B),
                    //right左边线
                    getLeft(AREA_R), getTop(AREA_R),
                    getLeft(AREA_R), getBottom(AREA_R)
                ), paint
            )

            drawTextInCenter(canvas, "left", getCenterX(AREA_L), getCenterY(AREA_L), paint)
            drawTextInCenter(canvas, "top", getCenterX(AREA_T), getCenterY(AREA_T), paint)
            drawTextInCenter(canvas, "center", getCenterX(AREA_C), getCenterY(AREA_C), paint)
            drawTextInCenter(canvas, "right", getCenterX(AREA_R), getCenterY(AREA_R), paint)
            drawTextInCenter(canvas, "bottom", getCenterX(AREA_B), getCenterY(AREA_B), paint)
        }
    }


    private fun drawTextInCenter(canvas: Canvas, text: String, cx: Float, cy: Float, paint: Paint) {
        val textWidth = paint.measureText(text)
        val fontMetrics = paint.fontMetrics
        val baseLineY = cy + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        canvas.drawText(text, cx - textWidth / 2, baseLineY, paint)
    }

    private fun drawFlipPath(canvas: Canvas) {
        if (!pointClick.isSet()) {
            return
        }
        //点击点所在区域的订点t
        val pointEnd = getEndPointInArea(pointClick) ?: return
        limitPointEnd(pointEnd)

        val isRight = pointStart.x < pointEnd.x
        val isTop = pointStart.y >= pointEnd.y
        val widthF = width.toFloat()
        val heightF = height.toFloat()

        //点击点a
        val aX = pointStart.x
        val aY = pointStart.y
        //顶点b
        val bX = pointEnd.x
        val bY = pointEnd.y

        canvas.drawLine(aX, aY, bX, bY, lineHelperPaint())
        canvas.drawText("a", aX, aY, textPaint())
        canvas.drawText(
            "b",
            if (isRight) bX - 30f else bX,
            if (isTop) bY + 30f else bY,
            textPaint()
        )

        //点击点a与顶点b的中心点c
        val cX = if (isRight) ((bX - aX) / 2f + aX) else ((aX - bX) / 2f)
        val cY = if (isTop) ((aY - bY) / 2f) else ((bY - aY) / 2f + aY)
        canvas.drawText("c", cX, cY, textPaint())

        //中心点与底边垂直相交的点c1
        paint.color = Color.GRAY
        paint.pathEffect = dashPathEffect
        val c1X = cX
        val c1Y = if (isTop) 0f else heightF
        canvas.drawLine(cX, cY, cX, c1Y, lineHelperPaint())
        canvas.drawText("c1", c1X, if (isTop) c1Y + 30f else c1Y, textPaint())

        //获取点击点a与订单中线左侧点
        val dis_c_c1 = posDis(c1Y, cY)
        //通过欧几里德定理获取c1左侧的长度
        //(c-c1)^2 = (d-c1)*(c1-b)

        val dis_c1_b = if (isRight) widthF - c1X else c1X
        val dis_d_c1 = dis_c_c1 * dis_c_c1 / dis_c1_b
        val dX = if (isRight) c1X - dis_d_c1 else c1X + dis_d_c1
        val dy = if (isTop) 0f else heightF
        canvas.drawLine(cX, cY, dX, dy, linePaint())
        canvas.drawText("d", dX, if (isTop) dy + 40f else dy, textPaint())
        //中线右侧点e
        //同理获取e
        val dis_c2_e = dis_c1_b * dis_c1_b / dis_c_c1

        val eX = if (isRight) widthF else 0f
        val eY = if (isTop) cY + dis_c2_e else cY - dis_c2_e
        canvas.drawLine(cX, cY, eX, eY, linePaint())
        canvas.drawText("e", if (isRight) eX - 20f else eX, eY, textPaint())

        //点击点a与中线点c的中点
        val fX = (if (isRight) aX else cX) + posDis(aX, cX) / 2f
        val fY = (if (isTop) cY else aY) + posDis(aY, cY) / 2f
        canvas.drawText("f", fX, fY, textPaint())

        //获取f平行d-e线与边线的交点g、h
        //因为f是a-b的1/4处
        val gX = if (isRight) widthF - posDis(dX, bX) * 3f / 2f else posDis(dX, bX) * 3f / 2f
        val gY = if (isTop) 0f else heightF
        val hX = if (isRight) widthF else 0f
        val hY = if (isTop) posDis(eY, bY) * 3f / 2f else heightF - posDis(eY, bY) * 3f / 2f
        canvas.drawLine(gX, gY, hX, hY, linePaint())

        //a-d与g-h的两个相交点

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
        paint.pathEffect = null
        return paint
    }

    private fun linePaint(): Paint {
        paint.color = Color.GREEN
        paint.pathEffect = dashPathEffect
        return paint
    }

    private fun textPaint(): Paint {
        paint.color = Color.RED
        paint.pathEffect = null
        return paint
    }

    private fun lineHelperPaint(): Paint {
        paint.color = Color.GRAY
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                pointClick.reset(event.x, event.y)
                pointStart.reset(pointClick)
            }
            MotionEvent.ACTION_MOVE -> {
                pointStart.reset(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                /*pointClick.reset(unset, unset)*/
            }
        }
        invalidate()
        return true
    }

    @IntDef(AREA_B, AREA_R, AREA_T, AREA_L, AREA_C)
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Area
}