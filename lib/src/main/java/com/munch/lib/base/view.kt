package com.munch.lib.base

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.children
import com.munch.lib.R

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
 * 给[View]添加四个方向的margin值，如果某个值未设置，则不更改其原本的值
 *
 * 如果[View]有[View.getLayoutParams]，则优先使用其值，否则使用[lpIfNo]，否则新建一个[ViewGroup.LayoutParams.WRAP_CONTENT]的[ViewGroup.MarginLayoutParams]
 */
fun View.addMargin(
    t: Int = 0,
    l: Int = 0,
    r: Int = 0,
    b: Int = 0,
    lpIfNo: ViewGroup.LayoutParams? = null
) {
    val lp = ViewHelper.newMarginLP(view = this, lpIfNo = lpIfNo)
    lp.topMargin += t
    lp.leftMargin += l
    lp.rightMargin += r
    lp.bottomMargin += b
    layoutParams = lp
}

fun View.setPaddingCompat(
    l: Int = paddingLeft,
    t: Int = paddingTop,
    r: Int = paddingRight,
    b: Int = paddingBottom
) = setPadding(l, t, r, b)

fun View.addPadding(l: Int = 0, t: Int = 0, r: Int = 0, b: Int = 0) = setPadding(
    paddingLeft + l,
    paddingTop + t,
    paddingRight + r,
    paddingBottom + b
)

fun View.setDoubleClickListener(time: Long = 300L, listener: View.OnClickListener) {
    setOnClickListener {
        val lastClickTime = it.getTag(ViewHelper.viewClickTimeId) as? Long?
        if (lastClickTime != null && System.currentTimeMillis() - lastClickTime <= time) {
            listener.onClick(it)
            return@setOnClickListener
        }
        it.setTag(ViewHelper.viewClickTimeId, System.currentTimeMillis())
    }
}

fun ViewGroup.clickItem(vararg clazz: Class<out View>, onClick: OnViewIndexClick) {
    val listener = onClick.toClickListener()
    var index = 0
    children.forEach { view ->
        if (clazz.isNotEmpty() && !isAssignable(clazz, view)) {
            return@forEach
        }
        view.tag = index
        index++
        view.setOnClickListener(listener)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun View.visible() {
    visibility = View.VISIBLE
}

@Suppress("NOTHING_TO_INLINE")
inline fun View.gone() {
    visibility = View.GONE
}

@Suppress("NOTHING_TO_INLINE")
inline fun View.invisible() {
    visibility = View.INVISIBLE
}

@Suppress("NOTHING_TO_INLINE")
inline fun View.removeParent() {
    (parent as? ViewGroup?)?.removeView(this)
}

fun isAssignable(clazz: Array<out Class<out Any>>, any: Any): Boolean {
    clazz.forEach { t ->
        if (t.isAssignableFrom(any::class.java)) {
            return true
        }
    }
    return false
}

fun EditText.showSoftInput() {
    isFocusable = true
    isFocusableInTouchMode = true
    postDelayed({
        requestFocus()
        requestFocusFromTouch()
        val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        im?.showSoftInput(this, 0)
    }, 300L)
}

fun EditText.hideSoftInput() {
    val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    im?.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * 要求父类不拦截触摸事件
 */
fun View.requestTouchEvent(request: Boolean) {
    (parent as? ViewGroup)?.requestDisallowInterceptTouchEvent(request)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ViewGroup.LayoutParams.toFrame() = FrameLayout.LayoutParams(this)

object ViewHelper {

    val viewClickTimeId = R.id.clickTime

    /**
     * 新建一个有margin值的[ViewGroup.MarginLayoutParams]
     *
     * @param view 如果传入[view]且不为null，则优先使用[view]的[View.getLayoutParams]来构建[ViewGroup.MarginLayoutParams]
     * @param lpIfNo 如果[view]为null，则尝试使用此值作为默认值来构建，
     * 如果此值也为null，则新建一个宽高为[ViewGroup.LayoutParams.WRAP_CONTENT]的[ViewGroup.MarginLayoutParams]
     * @param t 当margin是从view获取的且传入参数为null时，不会更改原值，否则为0
     *
     * @return 最后构建的[ViewGroup.MarginLayoutParams]
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

    fun newMMLayoutParams() = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

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
    ): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        val f = floatArrayOf(tl, tl, tr, tr, bl, bl, br, br)
        gradientDrawable.cornerRadii = f
        gradientDrawable.setColor(color)
        gradientDrawable.setStroke(strokeWidth, strokeColor)
        return gradientDrawable

    }

    fun newSelectDrawable(
        unselectedDrawable: Drawable?,
        selectedDrawable: Drawable?
    ): StateListDrawable {
        val drawable = StateListDrawable()
        drawable.addState(intArrayOf(-android.R.attr.state_selected), unselectedDrawable)
        drawable.addState(intArrayOf(android.R.attr.state_selected), selectedDrawable)
        return drawable
    }

}