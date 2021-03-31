package com.munch.lib.fast.base

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Create by munch1182 on 2021/3/31 10:28.
 */
open class BaseActivity : AppCompatActivity() {

    fun toast(msg: String) {
        runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    }
}