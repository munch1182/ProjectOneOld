package com.munch.test.view.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import com.munch.lib.libnative.helper.ResHelper
import com.munch.test.R

/**
 * Create by munch on 2020/9/9 18:02
 */
class BgTextView(context: Context, attrs: AttributeSet?, styleDef: Int) :
    AppCompatTextView(context, attrs, styleDef) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val radius = 30f

    init {
        paint.color = ResHelper.getColor(resId = R.color.colorPrimary)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawRoundRect(
            2f,
            2f,
            measuredWidth.toFloat() - 2f,
            measuredHeight.toFloat() - 2f,
            radius,
            radius,
            paint
        )
    }
}