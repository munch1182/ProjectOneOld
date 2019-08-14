package com.munch.weight.itemview

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.munch.lib.log.LogLog

/**
 * 失败：原因：无法通过自定义属性传递style集合，若是单个设置不如在xml中设置style
 * Created by Munch on 2019/8/10 16:19
 */
class ItemView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ItemView, defStyleAttr, 0)
        val startIconStyleResId =
            typedArray.getResourceId(R.styleable.ItemView_start_icon_style, R.style.ItemStartIconDef)

        val startIcon = typedArray.getDrawable(R.styleable.ItemView_start_icon)
        val startText = typedArray.getString(R.styleable.ItemView_start_text)
        val startTextStyleResId =
            typedArray.getResourceId(R.styleable.ItemView_start_text_style, 0)

        typedArray.recycle()

        if (startIcon != null || startIconStyleResId != R.style.ItemStartIconDef) {
            val startIconStyle =
                context.obtainStyledAttributes(startIconStyleResId, IntArray(1) { android.R.attr.layout_width })
            val width = startIconStyle.getDimensionPixelSize(0, 0)
            startIconStyle.recycle()
            val imageView = ImageView(ContextThemeWrapper(context, startIconStyleResId))
            imageView.setImageDrawable(startIcon)

            LogLog.log(width)

            addView(imageView, width, LayoutParams.WRAP_CONTENT)
        }

        if (!TextUtils.isEmpty(startText)) {

            val startTextView = TextView(
                if (0 != startTextStyleResId) ContextThemeWrapper(context, startTextStyleResId) else context
            )
            startTextView.text = startText
            addView(startTextView)
        }
    }


    companion object {

        fun getDrawable(context: Context, resId: Int): Drawable? {
            return ContextCompat.getDrawable(context, resId)
        }
    }

}