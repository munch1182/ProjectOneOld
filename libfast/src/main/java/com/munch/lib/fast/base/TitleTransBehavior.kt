package com.munch.lib.fast.base

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.munch.lib.log.log

/**
 * Create by munch1182 on 2021/8/11 16:37.
 */
class TitleTransBehavior(context: Context?, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<TextView>(context, attrs) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: TextView,
        dependency: View
    ): Boolean {
        return dependency is NestedScrollView
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: TextView,
        dependency: View
    ): Boolean {
        child.translationY = -dependency.scrollY.toFloat()
        log(dependency.scrollY)
        return true
    }
}
