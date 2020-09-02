package com.munch.test.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.munch.lib.libnative.helper.ResHelper
import com.munch.lib.log.LogLog
import com.munch.test.R

/**
 * Create by munch on 2020/9/2 10:39
 */
class TransferTitleBehavior(context: Context?, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<TextView>(context, attrs) {

    private var originalHeadX = 0
    private var originalHeadY = 0
    private var textFinSize = 0.0f
    private var textStartSize = 30f
    private var targetX = 0.0f
    private var targetY = 0.0f

    @SuppressLint("SetTextI18n")
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: TextView,
        dependency: View
    ): Boolean {

        val titleView =
            (parent.getChildAt(2) as ViewGroup?)?.getChildAt(1) as TextView? ?: return false
        titleView.visibility = View.INVISIBLE
        titleView.text = "122222"
        textFinSize = 20f
        targetX = titleView.x
        targetY = titleView.y
        return dependency is ImageView
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: TextView,
        dependency: View
    ): Boolean {

        var percentScale = dependency.y / dependency.height
        if (percentScale >= 1f) {
            percentScale = 1f
        }

        val scale = 1 - percentScale
        var size = textStartSize * scale
        if (size <= textFinSize) {
            size = textFinSize
        }

        child.textSize = size

        if (originalHeadX == 0) {
            originalHeadX = dependency.width / 2 - child.width*2 / 3
        }
        if (originalHeadY == 0) {
            originalHeadY = dependency.height - child.height *  2
        }
        val percentX = dependency.y / originalHeadX * 2
        val percentY = dependency.y / originalHeadY * 2
        var x = originalHeadX - originalHeadX * percentX
        if (x <= targetX) {
            x = targetX
        }
        var y = originalHeadY - originalHeadY * percentY
        if (y <= targetY) {
            y = targetY
        }

        child.x = x
        child.y = y


        //一种思路
        /*//展开时的位置
         //缩放
        var percentScale = dependency.y / dependency.height
        if (percentScale >= 1f) {
            percentScale = 1f
        }
        val scale = 1 - (0.5f * percentScale)
        child.scaleX = scale
        child.scaleY = scale

        if (originalHeadX == 0) {
            originalHeadX = dependency.width / 2 - child.width / 2
        }
        if (originalHeadY == 0) {
            originalHeadY = dependency.height - child.height * 3 / 2
        }
        var percentX = dependency.y / originalHeadX
        if (percentX >= 1f) {
            percentX = 1f
        }
        var percentY = dependency.y / originalHeadY
        if (percentY >= 1f) {
            percentY = 1f
        }
        var x = originalHeadX - originalHeadX * percentX
        if (x <= 80f) {
            x = 80f
        }

        child.x = x
        child.y = originalHeadY - originalHeadY * percentY*/


        return true
    }

}