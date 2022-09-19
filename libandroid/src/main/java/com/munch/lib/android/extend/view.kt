package com.munch.lib.android.extend

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 获取一个新的(WRAP_CONTENT,WRAP_CONTENT)的LayoutParams
 */
val newWWLP: ViewGroup.LayoutParams
    get() = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

/**
 * 获取一个新的(MATCH_PARENT,WRAP_CONTENT)的LayoutParams
 */
val newMWLP: ViewGroup.LayoutParams
    get() = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

val View.paddingHorizontal: Int
    get() = paddingLeft + paddingRight

val View.paddingVertical: Int
    get() = paddingTop + paddingBottom

/**
 * 设置View的点击效果, 会替换背景和顶部的Drawable
 */
fun View.clickEffect(color: Int = Color.WHITE) {
    background = ColorDrawable(color)
    foreground = getSelectableItemBackground()
}

//<editor-fold desc="gravity">
/**
 * 当前int值是否包含[gravity], 注意:并不是所有的[Gravity]都支持
 */
fun Int.hasGravity(gravity: Int): Boolean {
    return when (gravity) {
        Gravity.START, Gravity.END, Gravity.CENTER_HORIZONTAL -> this and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK == gravity
        Gravity.TOP, Gravity.BOTTOM, Gravity.CENTER_VERTICAL -> this and Gravity.VERTICAL_GRAVITY_MASK == gravity
        else -> false
    }
}

/**
 * 返回当前int值所包含的水平方向的[Gravity], 否则会返回Gravity.NO_GRAVITY
 */
fun Int.getHorizontalGravity(): Int {
    return when {
        hasGravity(Gravity.START) -> Gravity.START
        hasGravity(Gravity.END) -> Gravity.END
        hasGravity(Gravity.CENTER_HORIZONTAL) -> Gravity.CENTER_HORIZONTAL
        else -> Gravity.NO_GRAVITY
    }
}

/**
 * 返回当前int值所包含的垂直方向的[Gravity], 否则会返回Gravity.NO_GRAVITY
 */
fun Int.getVerticalGravity(): Int {
    return when {
        hasGravity(Gravity.TOP) -> Gravity.TOP
        hasGravity(Gravity.BOTTOM) -> Gravity.BOTTOM
        hasGravity(Gravity.CENTER_VERTICAL) -> Gravity.CENTER_VERTICAL
        else -> Gravity.NO_GRAVITY
    }
}
//</editor-fold>

/**
 * 双击回调
 * 两次点击之间间隔必须小于[doubleTime]
 *
 * 因为占用了点击事件, 所以不能再设计点击事件, 否则会被覆盖
 */
fun View.setDoubleClickListener(doubleTime: Long = 500L, l: View.OnClickListener?) {
    setOnClickListener(object : View.OnClickListener {
        private var lastClick = 0L
        override fun onClick(v: View?) {
            val now = System.currentTimeMillis()
            if (now - lastClick < doubleTime) {
                l?.onClick(v)
            }
            lastClick = now
        }
    })
}

/**
 * 一个使用颜色线分割的RecyclerView.ItemDecoration
 */
class LinearLineItemDecoration(
    private val lineHeight: Float = 1.5f,
    lineColor: Int = Color.parseColor("#f4f4f4")
) : RecyclerView.ItemDecoration() {

    private var lm: LinearLayoutManager? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = lineHeight
        color = lineColor
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        if (lm == null) {
            lm = parent.layoutManager?.to()
        }
        lm?.apply {
            val fp = findFirstVisibleItemPosition()
                .takeIf { it != RecyclerView.NO_POSITION }
                ?: return
            val ep = findLastVisibleItemPosition()
                .takeIf { it != RecyclerView.NO_POSITION }
                ?: return

            val l = parent.left.toFloat()
            val r = parent.right.toFloat()
            var y: Float
            for (i in fp..ep) {
                findViewByPosition(i)?.let {
                    y = it.top.toFloat()
                    c.drawLine(l, y, r, y, paint)
                }
            }
            if (fp == 0) {
                y = parent.top.toFloat() + lineHeight
                c.drawLine(l, y, r, y, paint)
            }
            if (ep == itemCount - 1) {
                y = parent.bottom.toFloat() - lineHeight
                c.drawLine(l, y, r, y, paint)
            }
        }
    }
}