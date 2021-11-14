package com.munch.lib.fast.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.fast.watcher.MeasureHelper

/**
 * Create by munch1182 on 2021/8/9 17:44.
 */
open class BaseActivity : AppCompatActivity() {

    private var init = false

    companion object {

        private const val KEY_MEASURE_ACTIVITY = "measure_activity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MeasureHelper.start(KEY_MEASURE_ACTIVITY)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (init || !hasFocus) {
            return
        }
        init = true
        MeasureHelper.cost(KEY_MEASURE_ACTIVITY, MeasureHelper.activityMeasureHelper) {
            MeasureHelper.log.log("${this::class.java.simpleName}: $it ms")
        }
    }

    /**
     * 此方法会晚于 [onResume] 执行
     */
    fun delayLoad(load: () -> Unit) {
        window.decorView.post { load.invoke() }
    }

    fun toast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

}

open class BaseFragment : BaseDBFragment()