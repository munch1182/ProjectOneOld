package com.munch.project.one.other

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat

/**
 * 给支持滑动的view上加入Header, 用于处理同步滑动
 *
 * 此Behavior必须使用在[CoordinatorLayout]中支持滑动的子view上, 会使用另一个子view作为header
 *
 * Create by munch1182 on 2022/9/30 9:56.
 */
class HeaderBehavior(context: Context, set: AttributeSet) :
    CoordinatorLayout.Behavior<View>(context, set) {

    private var headerHeight = 0

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {

        if (child.background == null) { // 未设置背景无法联动
            child.setBackgroundColor(Color.WHITE)
        }
        // child即此Behavior附着的目标, 此处为rv
        parent.onLayoutChild(child, layoutDirection)

        val header = findHeader(parent, child)
        headerHeight = header?.measuredHeight ?: 0

        ViewCompat.offsetTopAndBottom(child, headerHeight) // 移动初始显示, 不遮挡header
        return true
    }

    private fun findHeader(parent: CoordinatorLayout, child: View): View? {
        for (i in 0 until parent.childCount) { // 获取除child之外的另一个view作为header
            val view = parent.getChildAt(i)
            if (view == child) continue
            return view
        }
        return null
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll( // 在子控件处理滑动事件之前回调
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        if (headerHeight == 0) {
            return
        }
        if (dy > 0) { // 手指上滑
            val newTransY = child.translationY - dy // 因为translationY最开始为0, 随即为负数, 此处实际类累加
            if (newTransY >= -headerHeight) { // 向上移动还未移动完header的高度
                consumed[1] = dy    // 则产生的移动数据被消耗
                child.translationY = newTransY // 用来移动rv本身
            } else { // 如果向上移动已经移动完header的高度 // 此部分主要用来规整超出该高度的数据
                consumed[1] = headerHeight + child.translationY.toInt() // 实际消耗为0
                child.translationY = -headerHeight.toFloat() // 如果与上一个数据相同, translationY不会实际移动
            }
        }
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) { // 子控件滑动之后的回调，可以继续执行剩余距离
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed
        )
        if (dyUnconsumed < 0) { // 手指下滑, 即rv已经滑动到顶部 // yUnconsumed为负数
            val newTransY = child.translationY - dyUnconsumed // 因为translationY此时也为负数, 此处为累减
            if (newTransY <= 0) {
                child.translationY = newTransY
            } else {
                child.translationY = 0f
            }
        }
    }
}