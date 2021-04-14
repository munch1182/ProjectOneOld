package com.munch.test.project.one.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

/**
 * Create by munch1182 on 2021/1/12 17:34.
 */
class PorterDuffXfermodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private var mode: Xfermode? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        val radius = min(width, height) / 3f
        val side = radius * 3f / 2f

        val paddingLR = (width - radius - side) / 2f
        val paddingTB = (height - radius - side) / 2f

        paint.color = Color.YELLOW
        canvas.drawCircle(paddingLR + radius, paddingTB + radius, radius, paint)

        paint.xfermode = mode
        paint.color = Color.CYAN
        path.reset()
        path.moveTo(paddingLR + radius, paddingTB + radius)
        path.lineTo(paddingLR + radius, paddingTB + radius + side)
        path.lineTo(paddingLR + radius + side, paddingTB + radius + side)
        path.lineTo(paddingLR + radius + side, paddingTB + radius)
        path.close()
        canvas.drawPath(path, paint)
        paint.xfermode = null
    }

    fun changeXfermode(mode: Xfermode? = null, close: Boolean = false) {
        this.mode = mode
        if (close) {
            //关闭硬件加速
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
        invalidate()
    }
}