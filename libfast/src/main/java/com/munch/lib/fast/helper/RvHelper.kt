package com.munch.lib.fast.helper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2021/3/31 15:48.
 */
object RvHelper {

    fun newLineDecoration(
        @Px lineHeight: Int = 1,
        @ColorInt color: Int = Color.parseColor("#888888")
    ): RecyclerView.ItemDecoration {
        return object : RecyclerView.ItemDecoration() {

            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                setColor(color)
            }

            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(0, lineHeight, 0, 0)
            }

            override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                super.onDrawOver(c, parent, state)
                parent.children.forEach {
                    c.drawLine(
                        it.left.toFloat(), (it.top - 1).toFloat(),
                        it.right.toFloat(), (it.top - 1).toFloat(),
                        paint
                    )
                }
            }
        }
    }
}