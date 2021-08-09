package com.munch.lib.fast.base

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Create by munch1182 on 2021/8/9 17:44.
 */
open class BaseActivity : AppCompatActivity() {

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

open class BaseFragment : BaseDBFragment() {}