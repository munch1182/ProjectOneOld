package com.munch.project.one.other

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.munch.project.one.R

/**
 * Create by munch1182 on 2022/9/30 9:56.
 */
class FilterBehavior(context: Context, set: AttributeSet) :
    CoordinatorLayout.Behavior<View>(context, set) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return dependency is RecyclerView
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        // child即RecyclerView
        val filter = parent.findViewById<View>(R.id.bluetoothFilter)
        val height = filter?.measuredHeight ?: 0
        filter?.layout(
            parent.paddingStart,
            parent.paddingTop,
            parent.width - parent.paddingEnd,
            parent.paddingTop + height
        )

        val rv = parent.findViewById<View>(R.id.bluetoothRv)
        rv?.layout(
            parent.paddingStart,
            parent.paddingTop + height,
            parent.width - parent.paddingEnd,
            parent.height - parent.paddingBottom + height
        )
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
        consumed[0] = dx
        consumed[1] = dy
        ViewCompat.offsetTopAndBottom(child, dy)
        ViewCompat.offsetTopAndBottom(target, dy)
    }
}