package com.munch.lib.weight.debug

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.munch.lib.base.dp2Px
import com.munch.lib.weight.CircleViewHelper

/**
 * Create by munch1182 on 2021/11/12 09:25.
 */
class DebugLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : View(context, attrs, styleDef), CircleViewHelper {

    override val defWidth = context.dp2Px(64f).toInt()
    override val cw: Int
        get() = circleWidth.toInt()
    override val rect = RectF()
    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
            strokeWidth = circleWidth
            color = circleColor
            gradient?.let {
                val half = rect.width() / 2f
                gradientMatrix.setTranslate(half, half)
                it.setLocalMatrix(gradientMatrix)
                shader = it
            }
        }
    }
    private val gradientMatrix by lazy { Matrix() }
    private var rotate = 30f
        set(value) {
            field = if (value > 360) (value - 360) else value
        }

    private var circleWidth = context.dp2Px(4f)
    var circleColor = Color.GRAY
    var startColor = Color.WHITE
    var endColor = Color.parseColor("#28FFFFFF")

    //开始颜色和结束颜色要反向设置，因为转的方向的问题
    private var gradient: SweepGradient? = SweepGradient(0f, 0f, endColor, startColor)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val radius = measureRadius(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(radius, radius)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        measureRect(this, w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        //<editor-fold desc="debug">
        canvas.drawColor(Color.BLACK)

        paint.shader = null
        paint.strokeWidth = 3f
        paint.color = Color.RED
        canvas.drawRect(rect, paint)

        gradient?.let {
            val half = rect.width() / 2f
            gradientMatrix.setTranslate(half, half)
            it.setLocalMatrix(gradientMatrix)
            paint.shader = it
        }
        paint.strokeWidth = circleWidth
        paint.color = circleColor
        //</editor-fold>


        canvas.rotate(rotate, rect.centerX(), rect.centerY())

        canvas.drawArc(rect, 0f, 300f, false, paint)

        rotate += 10
        postInvalidateDelayed(16)
    }
}