package com.munch.lib.weight.color

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import com.munch.lib.weight.ViewHelper
import com.munch.lib.weight.ViewHelperDefault

open class ColorPlateOnly @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes), ViewHelper by ViewHelperDefault {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = getSquareRadius(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewRect(this, w, h)
    }

    protected open fun generateColorPlate(): Bitmap {
        val s = rectView.width()
        val r = s / 2f
        val bitmap = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)

        val colorAngleStep = 360 / 12
        val hsv = floatArrayOf(0f, 1f, 1f)
        val colors = IntArray(13) {
            hsv[0] = 360f - (it * colorAngleStep) % 360
            Color.HSVToColor(hsv)
        }
        val sweep = SweepGradient(r, r, colors, null)
        val radial = RadialGradient(
            r, r, r,
            "#ffffffff".toColorInt(), 0x00ffffff,
            Shader.TileMode.CLAMP
        )
        val compose = ComposeShader(sweep, radial, PorterDuff.Mode.SRC_OVER)
        paint.shader = compose
        Canvas(bitmap).drawCircle(r, r, r, paint)
        return bitmap
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        canvas.drawBitmap(generateColorPlate(), paddingLeft.toFloat(), paddingTop.toFloat(), paint)
    }

}