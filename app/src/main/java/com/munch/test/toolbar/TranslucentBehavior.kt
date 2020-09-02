package com.munch.test.toolbar

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout

/**
 * Create by munch on 2020/9/2 10:10
 */
class TranslucentBehavior(context: Context?, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<Toolbar>(context, attrs) {

    private var toolbarHeight = 0

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: Toolbar,
        dependency: View
    ): Boolean {
        return dependency is ImageView
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: Toolbar,
        dependency: View
    ): Boolean {

        //初始化高度
        if (toolbarHeight == 0) {
            //变得更慢
            toolbarHeight = child.bottom * 2
        }

        var percent = dependency.y / toolbarHeight

        if (percent >= 1) {
            percent = 1f
        }

        val alpha = percent * 255

        child.setBackgroundColor(Color.argb(alpha.toInt(), 0, 133, 119))

        return true
    }
}