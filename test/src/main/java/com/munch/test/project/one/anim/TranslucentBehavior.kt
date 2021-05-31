package com.munch.test.project.one.anim

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
    private var lastY = -1f

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: Toolbar,
        dependency: View
    ): Boolean {
        //初始化高度
        if (toolbarHeight == 0) {
            //变得更慢
            toolbarHeight = child.bottom * 2
        }
        return dependency is ImageView
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: Toolbar,
        dependency: View
    ): Boolean {

        if (lastY == dependency.y) {
            return true
        }
        lastY = dependency.y

        var percent = lastY / toolbarHeight

        if (percent >= 1f) {
            percent = 1f
        }

        val alpha = percent * 254f

        child.setBackgroundColor(Color.argb(alpha.toInt(), 3, 218, 197))

        return true
    }
}