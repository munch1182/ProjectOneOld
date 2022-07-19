package com.munch.lib.weight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible

open class ContainerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    protected open var target: View? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val child = getChild()
        if (child == null) {
            setMeasuredDimension(0, 0)
        } else {
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(child.measuredWidth, child.measuredHeight)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        getChild()?.layout(0, 0, r - l, b - t)
    }

    fun setTargetView(view: View) {
        target = view
    }

    protected open fun getView(): View? {
        return target ?: getChild() ?: findNearestView()
    }

    /**
     * 查找第一个显示的非FunctionalView的view
     */
    protected open fun getChild(): View? {
        val view = children.firstOrNull { it.isVisible && it !is FunctionalView } ?: return null
        target = view
        return target
    }

    /**
     * 查找此view相邻的view, 优先从下查找, 然后向上查找
     *
     * 会忽略FunctionalView, 但不会忽略隐藏的view
     */
    protected open fun findNearestView(): View? {
        val p = parent as? ViewGroup? ?: return null

        p.children.forEachIndexed { index, it ->
            if (it == this) {
                var i = index
                //优先向下匹配
                val count = p.childCount
                while (i < count - 1) {
                    i += 1
                    target = p.getChildAt(i)
                    if (target != null && target !is FunctionalView) {
                        return target
                    }
                }
                //否则向上匹配
                i = index
                while (i > 0) {
                    i -= 1
                    target = p.getChildAt(i)
                    if (target != null && target !is FunctionalView) {
                        return target
                    }
                }
                target = null
                return null
            }
        }
        return null
    }
}