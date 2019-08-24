package com.munch.project.one.base

import android.widget.Toast
import com.munch.lib.libnative.root.RootActivity

/**
 * Created by Munch on 2019/8/24 11:04
 */
abstract class BaseActivity : RootActivity() {

    override fun toast(message: String) {
        super.toast(message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}