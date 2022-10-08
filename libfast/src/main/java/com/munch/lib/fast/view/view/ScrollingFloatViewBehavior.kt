package com.munch.lib.fast.view.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import com.munch.lib.fast.view.findFirst
import kotlin.math.absoluteValue

/**
 * todo 需要更改 floadtView 的高度, 避免被遮挡
 *
 * Create by munch1182 on 2022/10/8 15:22.
 */
class ScrollingFloatViewBehavior(context: Context, set: AttributeSet) :
    CoordinatorLayout.Behavior<View>(context, set) {

    companion object {
        private fun findFloatView(parent: CoordinatorLayout): View? {
            return parent.findFirst<FloatViewBehavior>()
        }

        private fun findScroll(parent: CoordinatorLayout): View? {
            return parent.findFirst<ScrollingViewBehavior>().takeIf { it is ScrollingView }
        }
    }

    private var floatHeight = 0

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {

        parent.onLayoutChild(child, layoutDirection)

        val floatView = findFloatView(parent)
        floatHeight = floatView?.measuredHeight ?: 0

        ViewCompat.offsetTopAndBottom(child, floatHeight)
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
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        if (dy > 0) {
            val isWhenFloat = child.translationY.absoluteValue < floatHeight
            if (!isWhenFloat) { // 且只处理Float部分(当fFloat显示时child.translationY为Float)
                return
            }
            val preTranY = child.translationY - dy
            if (preTranY.absoluteValue <= floatHeight) {
                consumed[1] = dy
                child.translationY = preTranY
            } else {
                consumed[1] = child.translationY.toInt() + floatHeight
                child.translationY = -floatHeight.toFloat()
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
    ) {
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

        if (dyUnconsumed < 0) {
            val preTransY = child.translationY - dyUnconsumed // 因为translationY此时也为负数, 此处为累减
            if (preTransY <= 0) {
                child.translationY = preTransY
            } else {
                child.translationY = 0f
            }
        }
    }

    class FloatViewBehavior(context: Context, set: AttributeSet) :
        CoordinatorLayout.Behavior<View>(context, set) {

        private var transY = 0

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
            if (dependency.translationY.absoluteValue <= child.height) {
                show(child)
            }
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
        ) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

            if (dy > 0 && transY < 0) { // 如果是上滑且上一次记录的是下滑, 则重置
                transY = 0
            } else if (dy < 0 && transY > 0) { // 如果是下滑且上一次记录的是上滑, 则重置
                transY = 0
            } else { // 否则记录更改上下方向后未更改方向前的累计滑动距离
                transY += dy // 当dy为负值时transY为负值
            }

            val judgeDis = child.height * 2 // 用于判断是否切换显示的高度

            if (transY.absoluteValue > judgeDis) {
                if (transY > 0) { // 上滑
                    hide(child)
                } else if (transY < 0) { // 下滑
                    show(child)
                }
            }
        }

        private var runnable: AnimRunnable? = null

        private fun hide(child: View) {
            if (child.translationY < 0) {
                return
            }
            if (runnable == null) {
                runnable = AnimRunnable(child)
            }
            runnable?.hide()
        }

        private fun show(child: View) {
            if (child.translationY > -child.height) {
                return
            }
            if (runnable == null) {
                runnable = AnimRunnable(child)
            }
            runnable?.show()
        }

        private class AnimRunnable(private val view: View) : Runnable {

            private val scroller: OverScroller = OverScroller(view.context)

            override fun run() {
                if (scroller.computeScrollOffset()) {
                    view.translationY = scroller.currY.toFloat()
                    ViewCompat.postOnAnimation(view, this)
                }
            }

            fun hide() = autoMove(0, -view.height)

            fun show() = autoMove(-view.height, view.height)

            fun cancel() {
                if (!scroller.isFinished) {
                    scroller.abortAnimation()
                    view.removeCallbacks(this)
                }
            }

            private fun autoMove(from: Int, dy: Int) {
                if (scroller.isFinished) {
                    view.removeCallbacks(this)
                    scroller.startScroll(0, from, 0, dy)
                    ViewCompat.postOnAnimation(view, this)
                }
            }
        }
    }
}