package com.munch.lib.fast.view.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import com.munch.lib.fast.view.findFirst
import kotlin.math.absoluteValue

/**
 * 依附于[ScrollingView]的Behavior
 * Create by munch1182 on 2022/9/30 9:56.
 */
class ScrollingViewBehavior(context: Context, set: AttributeSet) :
    CoordinatorLayout.Behavior<View>(context, set) {

    companion object {
        private fun findHeader(parent: CoordinatorLayout): View? {
            return parent.findFirst<HeaderBehavior>()
        }

        private fun findFooter(parent: CoordinatorLayout): View? {
            return parent.findFirst<FooterBehavior>()
        }

        private fun findScroll(parent: CoordinatorLayout): View? {
            return parent.findFirst<ScrollingViewBehavior>().takeIf { it is ScrollingView }
        }
    }

    private var headerHeight = 0
    private var footerHeight = 0

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {

        parent.onLayoutChild(child, layoutDirection)

        val header = findHeader(parent)
        val footer = findFooter(parent)
        headerHeight = header?.measuredHeight ?: 0
        footerHeight = footer?.measuredHeight ?: 0
        ViewCompat.offsetTopAndBottom(child, headerHeight) // 移动初始显示, 不遮挡header
        return true
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

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) { // 在子控件处理滑动事件之前回调
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        if (dy > 0) { // 手指上滑
            val isWhenHeader = child.translationY.absoluteValue < headerHeight
            if (!isWhenHeader) { // 且只处理header部分(当footer显示时child.translationY为header+footer)
                return
            }
            val preTransY = child.translationY - dy // 因为translationY最开始为0, 随即为负数, 此处实际类累加
            if (preTransY.absoluteValue < headerHeight) { // 向上移动还未移动完header的高度
                consumed[1] = dy    // 则产生的移动数据被消耗
                child.translationY = preTransY // 用来移动rv本身
            } else { // 如果向上移动已经移动完header的高度 // 此部分主要用来规整超出该高度的数据
                consumed[1] = headerHeight + child.translationY.toInt() // 实际消耗为0
                child.translationY = -headerHeight.toFloat() // 如果与上一个数据相同, translationY不会实际移动
            }
        } else if (dy < 0) { // 手指下滑
            val isWhenFooter =
                child.translationY.absoluteValue > headerHeight // 当footer时child.translationY为header和footer高度和
            if (!isWhenFooter) { // 且只处理footer部分
                return
            }
            val preTransY = child.translationY - dy
            if (preTransY > -headerHeight && preTransY < 0) { // 向上移动还未移动完footer的高度(当移动距离相加时超过footer时newTransY>0)
                consumed[1] = dy
                child.translationY = preTransY
            } else {
                consumed[1] = headerHeight + child.translationY.toInt()
                child.translationY = -headerHeight.toFloat()
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
            val preTransY = child.translationY - dyUnconsumed // 因为translationY此时也为负数, 此处为累减
            if (preTransY <= 0) {
                child.translationY = preTransY
            } else {
                child.translationY = 0f
            }
        } else if (dyUnconsumed > 0) { // 向上滑动
            val preTransY = child.translationY - dyUnconsumed
            if (preTransY.absoluteValue < (headerHeight + footerHeight)) {
                consumed[1] = dyUnconsumed
                child.translationY = preTransY
            } else {
                child.translationY = (-headerHeight - footerHeight).toFloat()
            }
        }
    }

    /**
     * 显示在[ScrollingView]之前, 且当[ScrollingView]滑动到顶部时, 滑动并显示该view
     */
    class HeaderBehavior(context: Context, set: AttributeSet) :
        CoordinatorLayout.Behavior<View>(context, set) {

        override fun layoutDependsOn(
            parent: CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            return dependency is ScrollingView
        }

        override fun onDependentViewChanged(
            parent: CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            child.translationY = dependency.translationY
            return true
        }
    }

    /**
     * 显示在[ScrollingView]之后, 且当[ScrollingView]滑动到底部时, 滑动并显示该view
     */
    class FooterBehavior(context: Context, set: AttributeSet) :
        CoordinatorLayout.Behavior<View>(context, set) {

        override fun onLayoutChild(
            parent: CoordinatorLayout,
            child: View,
            layoutDirection: Int
        ): Boolean {
            parent.onLayoutChild(child, layoutDirection)

            val header = findHeader(parent)
            val scroll = findScroll(parent)
            val height = (header?.measuredHeight ?: 0) + (scroll?.measuredHeight ?: 0)

            ViewCompat.offsetTopAndBottom(child, height)
            return true
        }

        override fun layoutDependsOn(
            parent: CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            return dependency is ScrollingView
        }

        override fun onDependentViewChanged(
            parent: CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            child.translationY = dependency.translationY
            return true
        }
    }

}