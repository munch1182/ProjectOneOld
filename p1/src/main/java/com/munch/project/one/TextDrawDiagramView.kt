package com.munch.project.one

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.munch.lib.extend.drawGuidLine

/**
 * Created by munch1182 on 2022/4/4 10:25.
 */
class TextDrawDiagramView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : View(context, attrs, styleDef) {

    var s = "aghijlpqtyAFJQ"
    private val rect = Rect()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 56f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        paint.getTextBounds(s, 0, s.length, rect)
        setMeasuredDimension(rect.width(), rect.height())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        canvas.drawGuidLine(55f, 55f, 145f, 135f, null, paint)
    }
}