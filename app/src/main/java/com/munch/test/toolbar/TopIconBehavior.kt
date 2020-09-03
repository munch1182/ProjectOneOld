package com.munch.test.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.munch.lib.log.LogLog
import java.lang.NumberFormatException

/**
 * Create by munch on 2020/9/2 14:37
 */
class TopIconBehavior(context: Context?, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<LinearLayout>(context, attrs) {

    private var viewWidth = 0
    private var viewHeight = 0
    private var pos = -1
    private var viewMaxX = 0

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: LinearLayout,
        dependency: View
    ): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: LinearLayout,
        dependency: View
    ): Boolean {
        if (pos == -1) {
            pos = try {
                child.tag.toString().toInt()
            } catch (e: NumberFormatException) {
                0
            }
            viewWidth = dependency.width / 4
            viewHeight = child.height

            viewMaxX = viewWidth * pos
        }

        var percent = dependency.y / dependency.height

        if (percent >= 1f) {
            percent = 1f
        }
        if (percent  < 0f){
            percent = 0f
        }

        val params = child.layoutParams
        if (params != null) {
            params.width = (viewWidth * (1 - percent)).toInt()
            params.height = (viewHeight * (1 - percent)).toInt()
            child.requestLayout()
        }

        val textView = child.getChildAt(1)
        if (textView != null) {
            if (percent > 0.4f) {
                textView.alpha = 0f
            } else {
                textView.alpha = 1 - percent
            }
        }

        val imageView = child.getChildAt(0)
        if (imageView != null) {
            imageView.scaleX = 1 - 0.4f * percent
            imageView.scaleY = 1 - 0.4f * percent
        }

        child.x = viewMaxX - (viewMaxX - 50) * percent
        return true
    }
}