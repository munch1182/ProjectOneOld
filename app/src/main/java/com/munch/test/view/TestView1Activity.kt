package com.munch.test.view

import android.os.Bundle
import android.os.SystemClock
import com.munch.lib.log.LogLog
import com.munch.test.R
import com.munch.test.base.RvActivity
import kotlinx.android.synthetic.main.activity_test_view1.*
import java.util.concurrent.ThreadFactory
import kotlin.concurrent.thread

/**
 * Create by Munch on 2020/09/04
 */
class TestView1Activity : RvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_view1)

        setToolBar(view_tb)

        showProgress()

    }

    private fun showProgress() {
        thread {
            var progress = view_circle_progress.getProgress()
            while (progress < 100) {
                progress = getProgressInThread(progress)
                SystemClock.sleep(100)
                runOnUiThread {
                    view_circle_progress.setProgress(progress)
                }
            }
            runOnUiThread {
                view_circle_progress.setProgress(0f)
                showProgress()
            }
        }
    }


    private fun getProgressInThread(progress: Float): Float {
        return when {
            progress < 20f -> {
                progress + 0.3f
            }
            progress > 20f && progress < 50f -> {
                progress + 0.5f
            }
            progress > 50f && progress < 70f -> {
                progress + 0.8f
            }
            else -> {
                progress + 2f
            }
        }
    }
}