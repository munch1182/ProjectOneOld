@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Create by munch1182 on 2022/4/15 21:48.
 */
typealias ViewCreator = (context: Context) -> View

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

