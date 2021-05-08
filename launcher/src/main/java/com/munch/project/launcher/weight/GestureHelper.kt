package com.munch.project.launcher.weight

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import com.munch.pre.lib.log.Logger
import com.munch.project.launcher.base.LauncherApp
import kotlin.math.absoluteValue

/**
 * Create by munch1182 on 2021/5/8 14:10.
 */
class GestureHelper(context: Context) {

    private val gestureLog = Logger().apply {
        tag = "gesture"
        noInfo = true
        enable = LauncherApp.getInstance().debug()
    }

    private val detector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                /*if (distanceX.absoluteValue > distanceY.absoluteValue) {
                    if (distanceX < 0) fromL2R(distanceX) else fromR2L(distanceX)
                } else {
                    if (distanceY < 0) fromT2B(distanceY) else fromB2T(distanceY)
                }*/
                return false
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                e1 ?: return false
                e2 ?: return false
                val distanceX = e2.x - e1.x
                val distanceY = e2.y - e1.y
                if (distanceX.absoluteValue > distanceY.absoluteValue) {
                    if (distanceX < 0) flingFromL2R(distanceX) else flingFromR2L(distanceX)
                } else {
                    if (distanceY < 0) flingFromT2B(distanceY) else flingFromB2T(distanceY)
                }
                return true
            }
        })
    }


    private fun fromL2R(distance: Float) {
        gestureLog.log("L -> R : $distance")
    }

    private fun flingFromL2R(distance: Float) {
        gestureLog.log("fling L -> R : $distance")
    }

    private fun fromR2L(distance: Float) {
        gestureLog.log("R -> L : $distance")
    }

    private fun flingFromR2L(distance: Float) {
        gestureLog.log("fling R -> L : $distance")
    }

    private fun fromT2B(distance: Float) {
        gestureLog.log("T -> B : $distance")
    }

    private fun flingFromT2B(distance: Float) {
        gestureLog.log("fling T -> B : $distance")
    }

    private fun fromB2T(distance: Float) {
        gestureLog.log("B -> T : $distance")
    }

    private fun flingFromB2T(distance: Float) {
        gestureLog.log("fling B -> T : $distance")
    }

    fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return detector.onTouchEvent(ev)
    }
}