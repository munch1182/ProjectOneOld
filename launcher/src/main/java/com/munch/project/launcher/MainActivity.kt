package com.munch.project.launcher

import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.munch.project.launcher.appitem.AppActivity
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.databinding.ActivityMainBinding

/**
 * Create by munch1182 on 2021/2/23 14:41.
 */
class MainActivity : BaseActivity() {

    private val bind by bind<ActivityMainBinding>(R.layout.activity_main)
    private val gesture by lazy {
        GestureDetector(this, object :
            GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                e1 ?: return false
                e2 ?: return false
                if (e2.y - e1.y <= -300f) {
                    startAppActivity()
                    return true
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this

        bind.mainBtnApp.setOnClickListener {
            startAppActivity()
        }
    }

    private fun startAppActivity() {
        AppActivity.start(this)
    }

    override fun setPage(view: View) {
        super.setPage(view)
        window.navigationBarColor = Color.TRANSPARENT
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gesture.onTouchEvent(event)
    }
}