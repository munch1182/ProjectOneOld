package com.munch.lib.weight.shape

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.core.view.isVisible
import com.munch.lib.extend.OnUpdateListener
import com.munch.lib.extend.icontext.IContext
import com.munch.lib.extend.lazy
import com.munch.lib.weight.ContainerLayout
import com.munch.lib.weight.FunctionalView
import com.munch.lib.weight.R

class Shape @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ContainerLayout(context, attrs, defStyleAttr), FunctionalView, OnUpdateListener<Shape.Builder>,
    IContext {

    class Builder {
        var shape: Int = 0
        var radius: Float = 0f
        var topLeftRadius: Float = 0f
        var topRightRadius: Float = 0f
        var bottomLeftRadius: Float = 0f
        var bottomRightRadius: Float = 0f
        var color: Int = Color.WHITE
        var strokeColor: Int = Color.WHITE
        var strokeAlpha: Int = 0
        var strokeWidth: Float = 0f
        var strokeLineCap: Int = 0
        var strokeLineJoin: Int = 0
        var dashGap: Float = 0f
        var dashWidth: Float = 0f
        var angle: Float = 0f
        var gradientType: Int = -1
        var centerX: Float = 0f
        var centerY: Float = 0f
        var gradientRadius: Float = 0f
        var startColor: Int = Color.WHITE
        var centerColor: Int = Color.WHITE
        var endColor: Int = Color.WHITE
    }

    private val b = Builder()
    private val drawable by lazy { GradientDrawable() }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.Shape).apply {
            b.shape = this.getInteger(R.styleable.Shape_android_shape, 0)
            b.radius = this.getDimension(R.styleable.Shape_android_radius, 0f)
            b.topLeftRadius = this.getDimension(R.styleable.Shape_android_topLeftRadius, 0f)
            b.topRightRadius = this.getDimension(R.styleable.Shape_android_topRightRadius, 0f)
            b.bottomLeftRadius = this.getDimension(R.styleable.Shape_android_bottomLeftRadius, 0f)
            b.bottomRightRadius = this.getDimension(R.styleable.Shape_android_bottomRightRadius, 0f)
            b.color = this.getColor(R.styleable.Shape_android_color, Color.TRANSPARENT)
            b.strokeColor = this.getColor(R.styleable.Shape_android_strokeColor, Color.TRANSPARENT)
            b.strokeAlpha = this.getFloat(R.styleable.Shape_android_strokeAlpha, 0f).toInt()
            b.strokeWidth = this.getFloat(R.styleable.Shape_android_strokeWidth, 0f)
            b.strokeLineCap = this.getInteger(R.styleable.Shape_android_strokeLineCap, 0)
            b.strokeLineJoin = this.getInteger(R.styleable.Shape_android_strokeLineJoin, 0)
            b.dashGap = this.getDimension(R.styleable.Shape_android_dashGap, 0f)
            b.dashWidth = this.getDimension(R.styleable.Shape_android_dashWidth, 0f)
            b.angle = this.getFloat(R.styleable.Shape_android_angle, 0f)
            b.gradientType = this.getInteger(R.styleable.Shape_android_type, -1)
            b.centerX = this.getFloat(R.styleable.Shape_android_centerX, 0f)
            b.centerY = this.getFloat(R.styleable.Shape_android_centerY, 0f)
            b.gradientRadius = this.getFloat(R.styleable.Shape_android_gradientRadius, 0f)
            b.startColor = this.getColor(R.styleable.Shape_android_startColor, Color.TRANSPARENT)
            b.centerColor = this.getColor(R.styleable.Shape_android_centerColor, Color.TRANSPARENT)
            b.endColor = this.getColor(R.styleable.Shape_android_endColor, Color.TRANSPARENT)
        }.recycle()
    }

    override fun onUpdate(update: Builder.() -> Unit) {
        update.invoke(b)
        updateDrawable()
    }

    private fun updateDrawable() {
        val v = getView() ?: return
        drawable.shape = b.shape
        if (b.radius != 0f) {
            drawable.cornerRadius = b.radius
        } else {
            drawable.cornerRadii = floatArrayOf(
                b.topLeftRadius, b.topLeftRadius,
                b.topRightRadius, b.topRightRadius,
                b.bottomRightRadius, b.bottomRightRadius,
                b.bottomLeftRadius, b.bottomLeftRadius
            )
        }
        drawable.setColor(b.color)

        if (b.strokeWidth > 0f) {
            drawable.setStroke(b.strokeWidth.toInt(), b.strokeColor, b.dashWidth, b.dashGap)
        }
        //渐变
        if (b.gradientType != -1) {
            drawable.gradientType = b.gradientType
            drawable.setGradientCenter(b.centerX, b.centerY)
            drawable.gradientRadius = b.gradientRadius
            val angle = (b.angle % 360 / 45 * 45).toInt()
            drawable.orientation = when (angle) {
                0 -> GradientDrawable.Orientation.LEFT_RIGHT
                45 -> GradientDrawable.Orientation.BL_TR
                90 -> GradientDrawable.Orientation.BOTTOM_TOP
                135 -> GradientDrawable.Orientation.BR_TL
                180 -> GradientDrawable.Orientation.RIGHT_LEFT
                225 -> GradientDrawable.Orientation.TR_BL
                270 -> GradientDrawable.Orientation.TOP_BOTTOM
                315 -> GradientDrawable.Orientation.TL_BR
                else -> GradientDrawable.Orientation.LEFT_RIGHT
            }
            drawable.colors = intArrayOf(b.startColor, b.centerColor, b.endColor)
        }

        v.background = drawable
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (isVisible) {
            updateDrawable()
        }
    }

}