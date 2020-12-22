package com.munch.project.test.bar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.munch.lib.log
import com.munch.project.test.R

/**
 * Create by munch on 2020/9/2 10:39
 */
class TransferTitleBehavior(context: Context?, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<TextView>(context, attrs) {

    private var originalHeadX = 0
    private var originalHeadY = 0
    private var textFinSize = 0.0f
    private var textStartSize = 80f
    private var targetX = 0.0f
    private var targetY = 0.0f

    @SuppressLint("SetTextI18n")
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: TextView,
        dependency: View
    ): Boolean {
        //避免多次判断
        if (textFinSize != 0.0f && targetX != 0.0f && targetY != 0.0f) {
            return dependency is ImageView
        }
        //通过给toolbar设置文字的方式然后获取其TextView的位置来获取最终位置，一种取巧的方式
        val titleView =
            (parent.findViewById<Toolbar>(R.id.test_bar_tb))
                ?: return false
        titleView.title = "233"
        titleView.setTitleTextColor(Color.TRANSPARENT)
        if (titleView.childCount > 0) {
            titleView.getChildAt(0)?.takeIf { it is TextView }?.run {
                textFinSize = (this as TextView).textSize
                targetX = x
                targetY = y
            }
            log(targetX, targetY)
        }
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

        child.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)

        if (originalHeadX == 0) {
            originalHeadX = dependency.width / 2 - child.width * 2 / 3
        }
        if (originalHeadY == 0) {
            originalHeadY = dependency.height - child.height * 2
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