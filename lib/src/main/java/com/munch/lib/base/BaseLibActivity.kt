package com.munch.lib.base

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Create by munch1182 on 2020/12/7 10:45.
 */
open class BaseLibActivity : AppCompatActivity() {

    fun toast(msg: String) {
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

}