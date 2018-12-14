package com.munch.common.base.helper

import android.app.Activity
import com.gyf.barlibrary.BarHide
import com.gyf.barlibrary.ImmersionBar

/**
 * Created by Munch on 2018/12/8.
 */
object ViewHelper {

    fun hideStatusBar(activity: Activity) {
        ImmersionBar.with(activity).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).init()
    }

    fun hideNavigation(activity: Activity) {
        ImmersionBar.with(activity).hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR).init()
    }

    fun fullScreen(activity: Activity) {
        ImmersionBar.with(activity).fullScreen(true).hideBar(BarHide.FLAG_HIDE_BAR).init()
    }

    fun release(activity: Activity) {
        ImmersionBar.with(activity).destroy()
    }
}