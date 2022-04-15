@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.base.OnViewTagClickListener
import kotlin.reflect.KClass

/**
 * Create by munch1182 on 2022/4/15 21:48.
 */
typealias ViewCreator = (context: Context) -> View

interface OnViewUpdate<V : View> {

    fun updateView(update: V.() -> Unit)
}

/**
 * 注意可能会重复调用的情形
 */
inline fun View.addPadding(l: Int = 0, t: Int = 0, r: Int = 0, b: Int = 0) {
    setPadding(l + paddingLeft, t + paddingTop, r + paddingRight, b + paddingBottom)
}

inline fun newWWLp() = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
inline fun newMWLp() = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
inline fun newMMLp() = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

fun View.addMargin(l: Int = 0, t: Int = 0, r: Int = 0, b: Int = 0) {
    layoutParams = when (val lp = layoutParams) {
        null -> ViewGroup.MarginLayoutParams(newWWLp())
        is ViewGroup.MarginLayoutParams -> lp
        else -> ViewGroup.MarginLayoutParams(lp)
    }.apply {
        leftMargin += l
        topMargin += t
        rightMargin += r
        bottomMargin += b
    }
}

fun EditText.showSoftInput() {
    val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    im?.showSoftInput(this, 0)
}

fun EditText.hideSoftInput() {
    val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    im?.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * 生成一个圆角的Drawable
 *
 * @param color Drawable颜色
 * @param tl TopLeftRadius
 * @param tr BottomRightRadius
 * @param bl BottomLeftRadius
 * @param br BottomRightRadius
 * @param strokeWidth 边框宽度
 * @param strokeColor 边框颜色
 */
fun newCornerDrawable(
    color: Int,
    tl: Float = 0f,
    tr: Float = 0f,
    bl: Float = 0f,
    br: Float = 0f,
    strokeWidth: Int = 0,
    strokeColor: Int = Color.WHITE
) = GradientDrawable().apply {
    val f = floatArrayOf(tl, tl, tr, tr, bl, bl, br, br)
    cornerRadii = f
    setColor(color)
    setStroke(strokeWidth, strokeColor)
}

fun newSelectDrawable(unselectedDrawable: Drawable?, selectedDrawable: Drawable?) =
    StateListDrawable().apply {
        addState(intArrayOf(-android.R.attr.state_selected), unselectedDrawable)
        addState(intArrayOf(android.R.attr.state_selected), selectedDrawable)
    }

fun newEnableDrawable(disableDrawable: Drawable?, enableDrawable: Drawable?) =
    StateListDrawable().apply {
        addState(intArrayOf(-android.R.attr.state_enabled), disableDrawable)
        addState(intArrayOf(android.R.attr.state_enabled), enableDrawable)
    }

fun newPressedDrawable(unpressedDrawable: Drawable?, pressedDrawable: Drawable?) =
    StateListDrawable().apply {
        addState(intArrayOf(-android.R.attr.state_pressed), unpressedDrawable)
        addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
    }

fun ViewGroup.clickItem(listener: OnViewTagClickListener<Int>, vararg target: KClass<out View>) {
    var index = 0
    children.forEach { v ->
        if (!v.isVisible) {
            return@forEach
        }
        if (target.isNotEmpty()) {
            kotlin.run target@{
                target.forEach {
                    if (it.java.isAssignableFrom(v::class.java)) {
                        return@target
                    }
                }
                return@forEach
            }
        }
        v.tag = index
        v.setOnClickListener(listener)
        index++
    }
}

class LinearLineItemDecoration(
    private val lm: LinearLayoutManager,
    lineHeight: Float = 1f,
    lineColor: Int = Color.parseColor("#f4f4f4")
) : RecyclerView.ItemDecoration() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = lineHeight
        color = lineColor
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        lm.apply {
            val fp = findFirstVisibleItemPosition()
                .takeIf { it != RecyclerView.NO_POSITION }
                ?: return
            val ep = findLastVisibleItemPosition()
                .takeIf { it != RecyclerView.NO_POSITION }
                ?: return

            val l = parent.left.toFloat()
            val r = parent.right.toFloat()
            for (i in fp..ep) {
                findViewByPosition(i)?.let {
                    c.drawLine(l, it.top.toFloat(), r, it.top.toFloat(), paint)
                }
            }
            if (fp == 0) {
                c.drawLine(l, parent.top.toFloat(), r, parent.top.toFloat(), paint)
            }
            if (ep == itemCount - 1) {
                c.drawLine(l, parent.bottom.toFloat(), r, parent.bottom.toFloat(), paint)
            }
        }
    }
}

