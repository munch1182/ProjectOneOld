package com.munch.pre.lib.helper.measure

import android.app.Activity
import androidx.fragment.app.Fragment

/**
 * Create by munch1182 on 2021/5/8 18:12.
 */
class SimpleMeasureTime : MeasureTimeHelper() {

    companion object {

        const val MEASURE_ACTIVITY_SHOW = " create -> resume"
        const val MEASURE_FRAGMENT_SHOW = " layout inflate"
    }

    fun startActivityShow(activity: Activity) {
        start("${activity.javaClass.simpleName}$MEASURE_ACTIVITY_SHOW", 300L)
    }

    fun stopActivityShow(activity: Activity) {
        stop("${activity.javaClass.simpleName}$MEASURE_ACTIVITY_SHOW")
    }

    fun startFragmentShow(fragment: Fragment) {
        start("${fragment.javaClass.simpleName}$MEASURE_FRAGMENT_SHOW", 100L)
    }

    fun stopFragmentShow(fragment: Fragment) {
        stop("${fragment.javaClass.simpleName}$MEASURE_FRAGMENT_SHOW")
    }
}