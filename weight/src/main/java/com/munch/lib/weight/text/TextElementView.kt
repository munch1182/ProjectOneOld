package com.munch.lib.weight.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.munch.lib.extend.*
import kotlin.math.max
import kotlin.math.min

class TextElementView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val textPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = this@TextElementView.textSize
        }
    }
    private val linePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).setDashPath().apply { strokeWidth = 3f }
    }
    private val lineDescPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
        }
    }
    private val center = PointF()

    var textSize: Float = 50f
        set(value) {
            field = value
            textPaint.textSize = value
        }

    var str = "p长βбпㄎㄊěぬも┰┠№＠↓"

    private var descRightWidth = 0
    private var descBottomHeight = 0
    private var descPadding = 16

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        descRightWidth = lineDescPaint.measureText("**************************").toInt()
        descBottomHeight = lineDescPaint.getLineHeight().toInt()
        val w =
            textPaint.measureText(str).toInt() + paddingHorizontal() + descRightWidth + descPadding
        val h = textPaint.getLineHeight()
            .toInt() + paddingVertical() + descBottomHeight + descPadding

        val minW =
            lineDescPaint.measureText("********************************************************************************************************")
                .toInt()
        val maxW = MeasureSpec.getSize(widthMeasureSpec)
        val fw = min(max(w, minW), maxW)
        setMeasuredDimension(fw, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.set(
            (w - descRightWidth - descPadding) / 2f,
            (h - descBottomHeight - descPadding) / 2f
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        val fm = textPaint.fontMetrics
        val baseLineY = center.y + (fm.bottom - fm.top) / 2f - fm.bottom
        val topY = baseLineY + fm.top
        val ascentY = baseLineY + fm.ascent
        val descentY = baseLineY + fm.descent
        val bottomY = baseLineY + fm.bottom

        val sX = 0f
        val eX = width.toFloat() - descRightWidth
        var y: Float
        var desc: String
        var color: Int

        val sb = StringBuilder()
        sb.append("(")
            .append("base:")
            .append(baseLineY)
            .append(",")
        //是否相等与字体处理有关
        if (fm.top == fm.ascent) {
            sb.append("top/ascent:")
                .append(fm.top)
                .append(",")
        } else {
            sb.append("top:")
                .append(fm.top)
                .append(",")
                .append("ascent:")
                .append(fm.ascent)
                .append(",")
        }
        if (fm.bottom == fm.descent) {
            sb.append("bottom/descent:")
                .append(fm.bottom)
                .append(")")
        } else {
            sb.append("bottom:")
                .append(fm.bottom)
                .append(",")
                .append("descent:")
                .append(fm.descent)
                .append(")")
        }
        canvas.drawTextInCenter(
            sb.toString(),
            center.x,
            bottomY + descBottomHeight / 2 + descPadding,
            lineDescPaint
        )

        run {
            color = Color.RED
            color.descColor()

            y = baseLineY
            canvas.drawLine(sX, y, eX, y, linePaint)

            desc = "baseLine"
            canvas.drawTextInYCenter(desc, eX + descPadding, y, lineDescPaint)
        }

        if (topY == ascentY) {
            run {
                color = Color.BLUE
                color.descColor()

                y = topY
                canvas.drawLine(sX, y, eX, y, linePaint)

                desc = "top/ascent"
                canvas.drawTextInYCenter(desc, eX + descPadding, y, lineDescPaint)
            }
        } else {
            run {
                color = Color.BLUE
                color.descColor()

                y = topY
                canvas.drawLine(sX, y, eX, y, linePaint)

                desc = "top"
                canvas.drawTextInYCenter(desc, eX + descPadding, y, lineDescPaint)
            }

            run {
                color = Color.BLUE
                color.descColor()

                y = ascentY
                canvas.drawLine(sX, y, eX, y, linePaint)

                desc = "ascent"
                canvas.drawTextInYCenter(desc, eX + descPadding, y, lineDescPaint)
            }
        }

        if (bottomY == descentY) {
            run {
                color = Color.BLUE
                color.descColor()

                y = descentY
                canvas.drawLine(sX, y, eX, y, linePaint)

                desc = "bottom/descent"
                canvas.drawTextInYCenter(desc, eX + descPadding, y, lineDescPaint)
            }
        } else {
            run {
                color = Color.BLUE
                color.descColor()

                y = descentY
                canvas.drawLine(sX, y, eX, y, linePaint)

                desc = "descent"
                canvas.drawTextInYCenter(desc, eX + descPadding, y, lineDescPaint)
            }

            run {
                color = Color.GREEN
                color.descColor()

                y = bottomY
                canvas.drawLine(sX, y, eX, y, linePaint)
                desc = "bottom"
                canvas.drawTextInYCenter(desc, eX + descPadding, y, lineDescPaint)
            }
        }

        canvas.drawTextInCenter(str, center.x, center.y, textPaint)
    }

    private fun Int.descColor() {
        linePaint.color = this
        lineDescPaint.color = this
    }
}