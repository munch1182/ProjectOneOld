package com.munch.lib.fast.base

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.munch.lib.fast.R
import com.munch.lib.log.log

/**
 * Create by munch1182 on 2021/8/11 16:37.
 */
class TitleSwitchBehavior(context: Context?, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<TextView>(context, attrs) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: TextView,
        dependency: View
    ): Boolean {
        return dependency is TextView
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: TextView,
        layoutDirection: Int
    ): Boolean {
        val backView = parent.findViewById<View>(R.id.title_back)
        backView.setBackgroundColor(Color.RED)
        child.layout(
            backView.right,
            backView.top,
            backView.right + child.measuredWidth,
            backView.top + child.measuredHeight
        )
        child.x = backView.right.toFloat()
        child.y = -(backView.top.toFloat() - child.y)
        log(backView.right.toFloat(), child.x)
        log(backView.top.toFloat(), child.y)
        return true
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: TextView,
        dependency: View
    ): Boolean {
        log(child, dependency)
        return super.onDependentViewChanged(parent, child, dependency)
    }
}