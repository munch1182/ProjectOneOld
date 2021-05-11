package com.munch.lib.fast.base.activity

import android.os.Bundle
import com.munch.lib.fast.base.FastApp
import com.munch.pre.lib.base.BaseRootActivity

/**
 * Create by munch1182 on 2021/3/31 10:28.
 */
open class BaseActivity : BaseRootActivity() {

    protected open val measureTime = FastApp.measureHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        measureTime.startActivityShow(this)
        super.onCreate(savedInstanceState)
        delayLoad { measureTime.stopActivityShow(this) }
    }
}