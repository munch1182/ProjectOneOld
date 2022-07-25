package com.munch.lib.weight.item

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.munch.lib.extend.icontext.IContext
import com.munch.lib.extend.lazy
import com.munch.lib.weight.R
import kotlin.math.max
import kotlin.math.min

class ItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes), IContext {

    class Builder {

        var titleIcon: Drawable? = null
        var titleIconWidth = 0f
        var titleIconHeight = 0f
        var titleIconMargin = 0f
        var titleIconMarginTop = 0f
        var titleIconMarginStart = 0f
        var titleIconMarginBottom = 0f
        var titleIconMarginEnd = 0f
        var title: String? = null

        @ColorInt
        var titleColor = Color.BLACK
        var titleSize = 20f
        var titleStyle = 0
        var textIcon: Drawable? = null
        var textIconWidth = 0f
        var textIconHeight = 0f
        var textIconMargin = 0f
        var textIconMarginTop = 0f
        var textIconMarginStart = 0f
        var textIconMarginBottom = 0f
        var textIconMarginEnd = 0f
        var text: String? = null

        @ColorInt
        var textColor = titleColor
        var textSize = titleSize
        var textStyle = titleStyle
    }

    private val b = Builder()
    private val titleIcon by lazy { AppCompatImageView(context) }
    private val title by lazy { AppCompatTextView(context) }
    private val text by lazy { AppCompatTextView(context) }
    private val textIcon by lazy { AppCompatTextView(context) }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ItemView).apply {
            b.titleIcon = getDrawable(R.styleable.ItemView_titleIcon)
            b.titleIconWidth = getDimension(R.styleable.ItemView_titleIcon_width, b.titleIconWidth)
            b.titleIconHeight =
                getDimension(R.styleable.ItemView_titleIcon_height, b.titleIconHeight)
            b.titleIconMargin =
                getDimension(R.styleable.ItemView_titleIcon_margin, b.titleIconMargin)
            b.titleIconMarginStart =
                getDimension(R.styleable.ItemView_titleIcon_marginStart, b.titleIconMargin)
            b.titleIconMarginEnd =
                getDimension(R.styleable.ItemView_titleIcon_marginEnd, b.titleIconMargin)
            b.titleIconMarginTop =
                getDimension(R.styleable.ItemView_titleIcon_marginTop, b.titleIconMargin)
            b.titleIconMarginBottom =
                getDimension(R.styleable.ItemView_titleIcon_marginBottom, b.titleIconMargin)

            b.title = getString(R.styleable.ItemView_title)
            b.titleSize = getDimension(R.styleable.ItemView_title_textSize, b.titleSize)
            b.titleStyle = getInt(R.styleable.ItemView_title_textStyle, b.titleStyle)
            b.titleColor = getColor(R.styleable.ItemView_title_textColor, b.titleColor)

            b.textIcon = getDrawable(R.styleable.ItemView_textIcon)
            b.textIconWidth = getDimension(R.styleable.ItemView_textIcon_width, b.textIconWidth)
            b.textIconHeight =
                getDimension(R.styleable.ItemView_textIcon_height, b.textIconHeight)
            b.textIconMargin =
                getDimension(R.styleable.ItemView_textIcon_margin, b.textIconMargin)
            b.textIconMarginStart =
                getDimension(R.styleable.ItemView_textIcon_marginStart, b.textIconMargin)
            b.textIconMarginEnd =
                getDimension(R.styleable.ItemView_textIcon_marginEnd, b.textIconMargin)
            b.textIconMarginTop =
                getDimension(R.styleable.ItemView_textIcon_marginTop, b.textIconMargin)
            b.textIconMarginBottom =
                getDimension(R.styleable.ItemView_textIcon_marginBottom, b.textIconMargin)

            b.text = getString(R.styleable.ItemView_android_text)
            b.textSize = getDimension(R.styleable.ItemView_android_textSize, b.textSize)
            b.textStyle = getInt(R.styleable.ItemView_android_textStyle, b.textStyle)
            b.textColor = getColor(R.styleable.ItemView_android_textColor, b.textColor)
        }.recycle()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)

        removeAllViews()
        b.titleIcon?.let { addView(titleIcon) }
        b.title?.let { addView(title) }
        b.text?.let { addView(text) }
        b.textIcon?.let { addView(textIcon) }

        var maxW = 0f
        var maxH = 0f
        b.titleIcon?.let {
            titleIcon.background = it
            val titleIconWidth = if (b.titleIconWidth != 0f) b.titleIconWidth else w
            val titleIconHeight = if (b.titleIconHeight != 0f) b.titleIconHeight else h
            measureChild(
                titleIcon,
                MeasureSpec.makeMeasureSpec(titleIconWidth.toInt(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(titleIconHeight.toInt(), MeasureSpec.AT_MOST),
            )
            maxW += titleIcon.measuredWidth
            maxH =
                max(titleIcon.measuredHeight + b.titleIconMarginTop + b.titleIconMarginBottom, maxH)
        }
        b.title?.let {
            title.text = it
            title.setTextColor(b.textColor)
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, b.titleSize)

            when (b.titleStyle) {
                0 -> title.typeface = Typeface.DEFAULT
                1 -> title.typeface = Typeface.DEFAULT_BOLD
                2 -> title.typeface = Typeface.SANS_SERIF
            }
            measureChild(
                title,
                MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST),
            )
            maxW += title.measuredWidth
            maxH = max(title.measuredHeight.toFloat(), maxH)
        }
        b.text?.let {
            text.text = it
            text.setTextColor(b.textColor)
            text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, b.titleSize)

            when (b.titleStyle) {
                0 -> text.typeface = Typeface.DEFAULT
                1 -> text.typeface = Typeface.DEFAULT_BOLD
                2 -> text.typeface = Typeface.SANS_SERIF
            }
            measureChild(
                text,
                MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST),
            )
            maxW += text.measuredWidth
            maxH = max(text.measuredHeight.toFloat(), maxH)
        }
        b.textIcon?.let {
            textIcon.background = it
            val titleIconWidth = if (b.textIconWidth != 0f) b.textIconWidth else w
            val titleIconHeight = if (b.textIconHeight != 0f) b.textIconHeight else h
            measureChild(
                textIcon,
                MeasureSpec.makeMeasureSpec(titleIconWidth.toInt(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(titleIconHeight.toInt(), MeasureSpec.AT_MOST),
            )
            maxW += textIcon.measuredWidth
            maxH = max(textIcon.measuredHeight + b.textIconMarginTop + b.textIconMarginBottom, maxH)
        }
        maxW += b.textIconMarginStart + b.textIconMarginEnd + b.titleIconMarginStart + b.titleIconMarginEnd

        maxW += paddingLeft + paddingRight
        maxH += paddingTop + paddingBottom

        setMeasuredDimension(max(maxW.toInt(), w), min(maxH.toInt(), h))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val br = this.b
        var left = 0f
        var top: Float
        var right = 0f
        var bottom: Float
        br.titleIcon?.let {
            left += br.titleIconMarginStart + paddingLeft
            top = ((b - t) - titleIcon.measuredHeight) / 2f
            right = left + titleIcon.measuredWidth
            bottom = top + titleIcon.measuredHeight
            titleIcon.layout(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        }
        br.title?.let {
            left = right + br.titleIconMarginEnd
            top = ((b - t) - title.measuredHeight) / 2f
            right = left + title.measuredWidth
            bottom = top + title.measuredHeight
            title.layout(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

            title.setBackgroundColor(Color.RED)
        }

        val maxLeft = right
        left = r.toFloat() - paddingRight
        br.textIcon?.let {
            right = r - br.textIconMarginEnd - paddingRight
            top = ((b - t) - textIcon.measuredHeight) / 2f
            left = right - textIcon.measuredWidth
            bottom = top + textIcon.measuredHeight
            textIcon.layout(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        }
        br.text?.let {
            right = left - br.textIconMarginStart
            top = ((b - t) - text.measuredHeight) / 2f
            left = max(right - text.measuredWidth, maxLeft)
            bottom = top + text.measuredHeight
            text.layout(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

            text.setBackgroundColor(Color.YELLOW)
        }
    }

    fun getTitleIconView() = titleIcon
    fun getTitleView() = title
    fun getTextView() = text
    fun getTextIconView() = textIcon
}