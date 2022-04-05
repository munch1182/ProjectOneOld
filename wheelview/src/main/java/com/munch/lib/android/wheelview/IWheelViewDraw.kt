package com.munch.lib.android.wheelview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.munch.lib.android.extend.setDashPath
import kotlin.math.abs

/**
 * Created by munch1182 on 2022/4/3 21:49.
 */
interface IWheelViewDraw {

    /**
     * 绘制分割线
     *
     * @param paint 绘制分割线用paint，已经设置了设置的属性，可以更改
     * @param lines 分割线位置的坐标
     */
    fun onDrawLine(canvas: Canvas, paint: Paint, lines: FloatArray) {
        canvas.drawLines(lines, paint)
    }

    /**
     * 绘制文字
     *
     * @param paint 绘制文字用paint，已经设置了设置的属性，可以更改
     * @param maxWidth 当前文字一行能够绘制的最大宽度
     * @param centerPoint 当前绘制的item的中心点
     */
    fun onDrawText(
        text: String,
        canvas: Canvas,
        paint: TextPaint,
        maxWidth: Int,
        centerPoint: PointF
    ) {
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, maxWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 0.8f)
            .build()
        val baseLineY = abs(paint.ascent() + paint.descent()) / 2f
        canvas.save()
        canvas.translate(
            centerPoint.x - layout.width / 2f,
            centerPoint.y - layout.height / 2f
        )

        layout.draw(canvas)

        paint.setDashPath(Color.YELLOW)
        canvas.drawLine(
            layout.width / 2f,
            layout.height / 2f,
            layout.width.toFloat(),
            layout.height / 2f,
            paint
        )

        canvas.restore()
    }
}

class DefaultWheelViewDrawer : IWheelViewDraw