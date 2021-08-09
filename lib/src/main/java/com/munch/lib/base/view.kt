package com.munch.lib.base

import android.content.Context
import android.view.View
import android.view.ViewGroup

@FunctionalInterface
interface ViewCreator {

    fun create(context: Context): View
}

/**
 * 给[View]设置四个方向的margin值
 *
 * 如果[View]有[View.getLayoutParams]，则优先使用其值，否则使用[lpIfNo]，否则新建一个[ViewGroup.LayoutParams.WRAP_CONTENT]的[ViewGroup.MarginLayoutParams]
 */
fun View.setMargin(t: Int, l: Int, r: Int, b: Int, lpIfNo: ViewGroup.LayoutParams? = null) {
    val lp = getMarginLP(lpIfNo)
    lp.setMargins(t, l, r, b)
    layoutParams = lp
}

private fun View.getMarginLP(lpIfNo: ViewGroup.LayoutParams? = null): ViewGroup.MarginLayoutParams {
    return when (layoutParams) {
        null -> ViewGroup.MarginLayoutParams(
            lpIfNo ?: ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        is ViewGroup.MarginLayoutParams -> layoutParams as ViewGroup.MarginLayoutParams
        else -> ViewGroup.MarginLayoutParams(layoutParams)
    }
}

/**
 * 给[View]设置四个方向的margin值，如果某个值未设置或者为-1，则不更改其原本的值
 */
fun View.setMarginOrKeep(t: Int = -1, l: Int = -1, r: Int = -1, b: Int = -1) {
    val lp = getMarginLP()
    if (t != -1) {
        lp.topMargin = t
    }
    if (l != -1) {
        lp.leftMargin = l
    }
    if (r != -1) {
        lp.rightMargin = r
    }
    if (b != -1) {
        lp.bottomMargin = b
    }
    layoutParams = lp
}

/**
 * 给[View]添加四个方向的margin值，如果某个值未设置或者为-1，则不更改其原本的值
 */
fun View.addMargin(t: Int = -1, l: Int = -1, r: Int = -1, b: Int = -1) {
    val lp = getMarginLP()
    if (t != -1) {
        lp.topMargin += t
    }
    if (l != -1) {
        lp.leftMargin += l
    }
    if (r != -1) {
        lp.rightMargin += r
    }
    if (b != -1) {
        lp.bottomMargin += b
    }
    layoutParams = lp
}