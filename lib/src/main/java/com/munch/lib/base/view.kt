package com.munch.lib.base

import android.content.Context
import android.view.View
import android.view.ViewGroup

@FunctionalInterface
interface ViewCreator {

    fun create(context: Context): View
}

/**
 * 给[View]设置四个方向的margin值，如果某个值未设置，则不更改其原本的值
 *
 *  如果[View]有[View.getLayoutParams]，则优先使用其值，否则使用[lpIfNo]，否则新建一个[ViewGroup.LayoutParams.WRAP_CONTENT]的[ViewGroup.MarginLayoutParams]
 */
fun View.setMarginOrKeep(
    t: Int? = null,
    l: Int? = null,
    r: Int? = null,
    b: Int? = null,
    lpIfNo: ViewGroup.LayoutParams? = null
) {
    layoutParams = ViewHelper.newMarginLP(t, l, r, b, this, lpIfNo)
}

/**
 * 给[View]添加四个方向的margin值，如果某个值未设置或者为-1，则不更改其原本的值
 *
 * 如果[View]有[View.getLayoutParams]，则优先使用其值，否则使用[lpIfNo]，否则新建一个[ViewGroup.LayoutParams.WRAP_CONTENT]的[ViewGroup.MarginLayoutParams]
 */
fun View.addMargin(
    t: Int? = null,
    l: Int? = null,
    r: Int? = null,
    b: Int? = null,
    lpIfNo: ViewGroup.LayoutParams? = null
) {
    val lp = ViewHelper.newMarginLP(view = this, lpIfNo = lpIfNo)
    if (t != null) {
        lp.topMargin += t
    }
    if (l != null) {
        lp.leftMargin += l
    }
    if (r != null) {
        lp.rightMargin += r
    }
    if (b != null) {
        lp.bottomMargin += b
    }
    layoutParams = lp
}

object ViewHelper {

    /**
     * 新建一个有margin值的[ViewGroup.MarginLayoutParams]
     *
     * @param view 如果传入[view]且不为null，则优先使用[view]的[View.getLayoutParams]来构建[ViewGroup.MarginLayoutParams]
     * @param lpIfNo 如果[view]为null，则尝试使用此值作为默认值来构建，
     *  如果此值也为null，则新建一个宽高为[ViewGroup.LayoutParams.WRAP_CONTENT]的[ViewGroup.MarginLayoutParams]
     *
     *  @return 最后构建的[ViewGroup.MarginLayoutParams]
     *
     * @see getMarginLP
     */
    fun newMarginLP(
        t: Int? = null,
        l: Int? = null,
        r: Int? = null,
        b: Int? = null,
        view: View? = null,
        lpIfNo: ViewGroup.LayoutParams? = null
    ): ViewGroup.MarginLayoutParams {
        val lp = getMarginLP(view, lpIfNo)
        if (t != null) {
            lp.topMargin = t
        }
        if (l != null) {
            lp.leftMargin = l
        }
        if (r != null) {
            lp.rightMargin = r
        }
        if (b != null) {
            lp.bottomMargin = b
        }
        return lp
    }

    private fun getMarginLP(
        view: View? = null,
        lpIfNo: ViewGroup.LayoutParams? = null
    ): ViewGroup.MarginLayoutParams {
        return when (view?.layoutParams) {
            null -> ViewGroup.MarginLayoutParams(lpIfNo ?: newWWLayoutParams())
            is ViewGroup.MarginLayoutParams -> view.layoutParams as ViewGroup.MarginLayoutParams
            else -> ViewGroup.MarginLayoutParams(view.layoutParams)
        }
    }

    /**
     * [ViewGroup.LayoutParams.WRAP_CONTENT] + [ViewGroup.LayoutParams.WRAP_CONTENT]
     */
    fun newWWLayoutParams() = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    /**
     * [ViewGroup.LayoutParams.MATCH_PARENT] + [ViewGroup.LayoutParams.WRAP_CONTENT]
     */
    fun newMWLayoutParams() = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

}