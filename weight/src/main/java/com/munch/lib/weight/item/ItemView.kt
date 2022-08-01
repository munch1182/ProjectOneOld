package com.munch.lib.weight.item

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children
import com.munch.lib.extend.UpdateListener
import com.munch.lib.extend.icontext.IContext
import com.munch.lib.extend.icontext.dp2Px
import com.munch.lib.extend.lazy
import com.munch.lib.weight.ITextView
import com.munch.lib.weight.R
import com.munch.lib.weight.TouchHelper
import com.munch.lib.weight.TouchHelperDefault
import kotlin.math.max
import kotlin.math.min

open class ItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes), IContext, ITextView,
    TouchHelper by TouchHelperDefault, UpdateListener<ItemView.Builder> {

    class Builder {

        var titleIcon: Drawable? = null
        var titleIconWidth = 0f
        var titleIconHeight = 0f
        var titleIconMargin = 0f
        var titleIconMarginTop = 0f
        var titleIconMarginStart = 0f
        var titleIconMarginBottom = 0f
        var titleIconMarginEnd = 0f
        var titleIconTint = Color.TRANSPARENT
        var title: String? = null

        @ColorInt
        var titleColor = Color.BLACK
        var titleSize = 0f
        var titleStyle = 0
        var textIcon: Drawable? = null
        var textIconWidth = 0f
        var textIconHeight = 0f
        var textIconMargin = 0f
        var textIconMarginTop = 0f
        var textIconMarginStart = 0f
        var textIconMarginBottom = 0f
        var textIconMarginEnd = 0f
        var textIconTint = Color.TRANSPARENT
        var text: String? = null

        @ColorInt
        var textColor = titleColor
        var textSize = 0f
        var textStyle = titleStyle
    }

    protected open val idTag = com.munch.lib.R.id.id_tag
    protected open val b = Builder()
    protected open val titleIcon by lazy {
        AppCompatImageView(context).apply { setTag(idTag, true) }
    }
    protected open val title by lazy {
        AppCompatTextView(context).apply { setTag(idTag, true) }
    }
    protected open val text by lazy {
        AppCompatTextView(context).apply { setTag(idTag, true) }
    }
    protected open val textIcon by lazy {
        AppCompatImageView(context).apply { setTag(idTag, true) }
    }
    protected open var childView: View? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ItemView).apply {
            b.titleIcon = getDrawable(R.styleable.ItemView_item_titleIcon)
            b.titleIconWidth =
                getDimension(R.styleable.ItemView_item_titleIcon_width, b.titleIconWidth)
            b.titleIconHeight =
                getDimension(R.styleable.ItemView_item_titleIcon_height, b.titleIconHeight)
            b.titleIconMargin =
                getDimension(R.styleable.ItemView_item_titleIcon_margin, -1f)
            val titleMarginWidth = if (b.titleIconMargin == -1f) dp2Px(16f) else b.titleIconMargin
            val titleMarginHeight = if (b.titleIconMargin == -1f) dp2Px(8f) else b.titleIconMargin
            b.titleIconMarginStart =
                getDimension(R.styleable.ItemView_item_titleIcon_marginStart, titleMarginWidth)
            b.titleIconMarginEnd =
                getDimension(R.styleable.ItemView_item_titleIcon_marginEnd, titleMarginWidth)
            b.titleIconMarginTop =
                getDimension(R.styleable.ItemView_item_titleIcon_marginTop, titleMarginHeight)
            b.titleIconMarginBottom =
                getDimension(R.styleable.ItemView_item_titleIcon_marginBottom, titleMarginHeight)
            b.titleIconTint = getColor(R.styleable.ItemView_item_titleIcon_tint, b.titleIconTint)

            b.title = getString(R.styleable.ItemView_item_title)
            b.titleSize = getDimension(R.styleable.ItemView_item_title_textSize, b.titleSize)
            b.titleStyle = getInt(R.styleable.ItemView_item_title_textStyle, b.titleStyle)
            b.titleColor = getColor(R.styleable.ItemView_item_title_textColor, b.titleColor)

            b.textIcon = getDrawable(R.styleable.ItemView_item_textIcon)
            b.textIconWidth =
                getDimension(R.styleable.ItemView_item_textIcon_width, b.textIconWidth)
            b.textIconHeight =
                getDimension(R.styleable.ItemView_item_textIcon_height, b.textIconHeight)
            b.textIconMargin =
                getDimension(R.styleable.ItemView_item_textIcon_margin, -1f)
            val textMarginWidth = if (b.textIconMargin == -1f) dp2Px(16f) else b.textIconMargin
            val textMarginHeight = if (b.textIconMargin == -1f) dp2Px(8f) else b.textIconMargin
            b.textIconMarginStart =
                getDimension(R.styleable.ItemView_item_textIcon_marginStart, textMarginWidth)
            b.textIconMarginEnd =
                getDimension(R.styleable.ItemView_item_textIcon_marginEnd, textMarginWidth)
            b.textIconMarginTop =
                getDimension(R.styleable.ItemView_item_textIcon_marginTop, textMarginHeight)
            b.textIconMarginBottom =
                getDimension(R.styleable.ItemView_item_textIcon_marginBottom, textMarginHeight)
            b.textIconTint = getColor(R.styleable.ItemView_item_textIcon_tint, b.textIconTint)

            b.text = getString(R.styleable.ItemView_item_text)
            b.textSize = getDimension(R.styleable.ItemView_item_textSize, b.textSize)
            b.textStyle = getInt(R.styleable.ItemView_item_textStyle, b.textStyle)
            b.textColor = getColor(R.styleable.ItemView_item_textColor, b.textColor)
        }.recycle()
    }

    override fun setTextColor(color: Int) {
        update { this.textColor = color }
    }

    override fun setTextColor(color: ColorStateList) {
        text.setTextColor(color)
    }

    override fun update(update: Builder.() -> Unit) {
        update.invoke(b)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)

        var maxW = 0f
        var maxH = 0f

        if (childView == null) {
            childView = findChild()
        }
        removeAllViews()
        childView?.let { addView(it) }

        b.titleIcon?.let {
            addView(titleIcon)
            if (b.titleIconTint != Color.TRANSPARENT) it.setTint(b.titleIconTint)
            titleIcon.setImageDrawable(it)

            val titleIconWidth = if (b.titleIconWidth != 0f) b.titleIconWidth else w
            val titleIconHeight = if (b.titleIconHeight != 0f) b.titleIconHeight else h
            measureChild(
                titleIcon,
                MeasureSpec.makeMeasureSpec(
                    titleIconWidth.toInt() + paddingStart + paddingEnd,
                    MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(
                    titleIconHeight.toInt() + paddingTop + paddingBottom,
                    MeasureSpec.EXACTLY
                ),
            )
            maxW += titleIcon.measuredWidth
            maxH =
                max(titleIcon.measuredHeight + b.titleIconMarginTop + b.titleIconMarginBottom, maxH)
        }
        b.title?.let {
            addView(title)
            title.text = it
            title.setTextColor(b.titleColor)
            if (b.titleSize > 0f) title.setTextSize(TypedValue.COMPLEX_UNIT_PX, b.titleSize)

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
            addView(text)
            text.text = it
            text.setTextColor(b.textColor)
            if (b.textSize > 0f) text.setTextSize(TypedValue.COMPLEX_UNIT_PX, b.textSize)

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
            addView(textIcon)
            if (b.textIconTint != Color.TRANSPARENT) it.setTint(b.textIconTint)
            textIcon.setImageDrawable(it)

            val titleIconWidth = if (b.textIconWidth != 0f) b.textIconWidth else w
            val titleIconHeight = if (b.textIconHeight != 0f) b.textIconHeight else h
            measureChild(
                textIcon,
                MeasureSpec.makeMeasureSpec(
                    titleIconWidth.toInt() + paddingStart + paddingEnd,
                    MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(
                    titleIconHeight.toInt() + paddingStart + paddingEnd,
                    MeasureSpec.EXACTLY
                ),
            )
            maxW += textIcon.measuredWidth
            maxH = max(textIcon.measuredHeight + b.textIconMarginTop + b.textIconMarginBottom, maxH)
        }
        findChild()?.let {
            measureChild(
                it,
                MeasureSpec.makeMeasureSpec(w, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(maxH.toInt(), MeasureSpec.EXACTLY),
            )
            maxW += it.measuredWidth
            maxH = max(it.measuredHeight.toFloat(), maxH)
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
        }
        findChild()?.let {
            right = left - br.textIconMarginStart
            top = ((b - t) - it.measuredHeight) / 2f
            left = max(right - it.measuredWidth, maxLeft)
            bottom = top + it.measuredHeight
            it.layout(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        }
    }

    private fun findChild() =
        childView ?: children.find { it.getTag(idTag) == null }
            .apply { childView = this }

    override fun performClick(): Boolean {
        findChild()?.performClick()
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        updateEvent(event)
        if (event.action == MotionEvent.ACTION_UP && isClick) {
            performClick()
        }
        return super.onTouchEvent(event)
    }

    fun getTitleIconView() = titleIcon
    fun getTitleView() = title
    fun getTextView() = text
    fun getTextIconView() = textIcon
}