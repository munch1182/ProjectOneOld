package com.munch.lib.fast.base

import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.helper.measure.SimpleMeasureTime

/**
 * Create by munch1182 on 2021/5/11 12:00.
 */
open class FastApp : BaseApp() {

    companion object{
        val measureHelper = SimpleMeasureTime()
    }
}