package com.munch.lib.weight

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.munch.lib.R
import com.munch.lib.base.dp2Px
import com.munch.lib.base.drawTextInYCenter
import com.munch.lib.base.measureTextBounds
import com.munch.lib.base.sp2Px
import com.munch.lib.weight.LoadingView.StyleArc
import com.munch.lib.weight.LoadingView.StyleText

/**
 * 融合多个style的LoadingView
 *
 * @see StyleArc
 * @see StyleText
 *
 * Create by munch1182 on 2021/11/12 14:42.
 */
class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : View(context, attrs, styleDef), ViewUpdate<LoadingView> {

    companion object {
        const val STYLE_ARC = 10
        const val STYLE_TEXT = 20
        const val STYLE_TEXT_ONLY = 21
    }

    constructor(context: Context, style: Int) : this(context) {
        this.style = style
    }

    private var style: Int = STYLE_ARC
        set(value) {
            field = value
            setImpByStyle(context)
        }
    private var imp: ViewImp<LoadingView>? = null

    init {
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.LoadingView).apply {
                style = getInt(R.styleable.LoadingView_lv_style, style)
                imp?.obtainStyledAttributes(this)
            }.recycle()
        }
    }

    private fun setImpByStyle(context: Context) {
        imp = when (style) {
            STYLE_ARC -> StyleArc(context, this@LoadingView)
            STYLE_TEXT -> StyleText(context, this@LoadingView)
            STYLE_TEXT_ONLY -> StyleText(context, this@LoadingView, true)
            else -> null
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        imp?.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        imp?.onLayout(changed, left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imp?.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        imp?.onDraw(canvas)
    }

    fun changeStyle(style: Int) {
        this.style = style
        requestLayout()
    }

    override fun set(set: LoadingView.() -> Unit) {
        super.set(set)
        imp?.set(set)
    }

    private class StyleArc(context: Context, override val view: LoadingView) : ViewImp<LoadingView>,
        CircleViewHelper {
        override val defWidth = context.dp2Px(32f).toInt()
        override val cw: Int
            get() = circleWidth.toInt()
        override val rect = RectF()
        private val paint by lazy {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                strokeCap = Paint.Cap.ROUND
                style = Paint.Style.STROKE
                strokeWidth = circleWidth
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

        var circleWidth = context.dp2Px(4f)
        var startColor = Color.WHITE
        var endColor = Color.parseColor("#28FFFFFF")
        var speed = 16L

        //开始颜色和结束颜色要反向设置，因为转的方向的问题
        var gradient: SweepGradient? = null

        override fun obtainStyledAttributes(typedArray: TypedArray) {
            super.obtainStyledAttributes(typedArray)
            typedArray.apply {
                circleWidth = getDimension(R.styleable.LoadingView_strokeWidth, circleWidth)
                speed = getInt(R.styleable.LoadingView_lv_speed, speed.toInt()).toLong()
                startColor = getColor(R.styleable.LoadingView_lv_startColor, startColor)
                endColor = getColor(R.styleable.LoadingView_lv_endColor, endColor)
                gradient = SweepGradient(0f, 0f, endColor, startColor)
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            val radius = measureRadius(widthMeasureSpec, heightMeasureSpec)
            view.setMeasuredDimension(radius, radius)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            measureRect(view, w, h)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            canvas.rotate(rotate, rect.centerX(), rect.centerY())
            canvas.drawArc(rect, 0f, 280f, false, paint)

            rotate += 10
            view.postInvalidateDelayed(speed)
        }

        override fun set(set: LoadingView.() -> Unit) {
            super.set(set)
            view.invalidate()
        }
    }

    private class StyleText(
        context: Context,
        override val view: LoadingView,
        only: Boolean = false
    ) : ViewImp<LoadingView> {
        var speed = 500L
        var text = "加载中"
        var textLoading = if (only) "" else "..."
        var textColor = Color.BLACK
        var textSize = context.sp2Px(14f)
        private val paint by lazy {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = this@StyleText.textColor
                textSize = this@StyleText.textSize
            }
        }
        private var index = 1
            set(value) {
                if (value != index) {
                    field = if (value > maxIndex) 1 else value
                }
            }
        private var maxIndex = 0

        override fun obtainStyledAttributes(typedArray: TypedArray) {
            super.obtainStyledAttributes(typedArray)
            typedArray.apply {
                speed = getInt(R.styleable.LoadingView_lv_speed, speed.toInt()).toLong()
                text = getString(R.styleable.LoadingView_android_text) ?: text
                textLoading = getString(R.styleable.LoadingView_lv_loading_text) ?: textLoading
                textColor = getColor(R.styleable.LoadingView_android_textColor, textColor)
                textSize = getDimension(R.styleable.LoadingView_android_textSize, textSize)
            }
        }

        override fun set(set: LoadingView.() -> Unit) {
            super.set(set)
            view.invalidate()
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val maxStr = " $text$textLoading"
            index = 1
            maxIndex = textLoading.length
            paint.measureTextBounds(maxStr).apply {
                view.setMeasuredDimension(width() + 6, height())
            }
        }

        override fun onDraw(canvas: Canvas) {
            if (maxIndex == 0) {
                canvas.drawTextInYCenter(text, 0f, view.height / 2f, paint)
            } else {
                canvas.drawTextInYCenter(
                    "$text${textLoading.subSequence(0, index)}",
                    0f, view.height / 2f, paint
                )
                index++
                view.postInvalidateDelayed(speed)
            }
        }
    }
}