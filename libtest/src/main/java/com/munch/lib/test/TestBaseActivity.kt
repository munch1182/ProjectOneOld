package com.munch.lib.test

import android.widget.Toast
import com.munhc.lib.libnative.root.RootActivity

/**
 * Created by Munch on 2019/7/16 8:52
 */
open class TestBaseActivity : RootActivity() {

    open fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}