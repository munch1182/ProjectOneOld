package com.munch.pre.lib.helper

import android.graphics.Rect
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import com.munch.pre.lib.extend.obOnCreate
import com.munch.pre.lib.log.log

/**
 * Create by munch1182 on 2021/4/21 17:37.
 */
object ImHelper {

    const val DEF_SOFT_KEYBOARD_HEIGHT = 100

    /**
     * 通过根布局的可视区域与屏幕底部的差值判断是否弹出或者隐藏了im
     *
     * @param view 键盘弹起后会被遮挡的view
     * @param onChange 参数即差值
     */
    fun watchChange(
        activity: ComponentActivity,
        view: View = activity.findViewById(android.R.id.content),
        onChange: (diff: Int) -> Unit
    ) {
        val listener = {
            if (activity.lifecycle.currentState == Lifecycle.State.RESUMED) {
                val r = Rect()
                view.getWindowVisibleDisplayFrame(r)
                val heightDiff = view.bottom - r.bottom
                onChange.invoke(heightDiff)
            }
        }
        activity.obOnCreate({
            view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        }, {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        })
    }

    /**
     * 通过观察显示或者隐藏键盘时某些view的y坐标判断
     *
     * @param view 键盘弹起后会y值会有变化的view，比如FloatingActionButton
     * @param onChange 参数即差值
     */
    fun watchMoveViewChange(
        activity: ComponentActivity,
        view: View,
        onChange: (diff: Int) -> Unit
    ) {
        var start = view.y
        var diff = 0f
        val listener = {
            if (activity.lifecycle.currentState == Lifecycle.State.RESUMED) {
                val heightDiff = start - view.y
                if (diff != heightDiff) {
                    diff = heightDiff
                    onChange.invoke(heightDiff.toInt())
                }
            }
        }
        activity.obOnCreate({
            view.post {
                start = view.y
                view.viewTreeObserver.addOnGlobalLayoutListener(listener)
            }
        }, {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        })
    }
}