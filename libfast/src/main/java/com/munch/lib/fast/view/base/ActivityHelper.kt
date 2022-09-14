package com.munch.lib.fast.view.base

import com.munch.lib.android.helper.ActivityStackHelper
import com.munch.lib.fast.view.dispatch.DispatcherActivity

object ActivityHelper : ActivityStackHelper<DispatcherActivity>() {

    val curr: DispatcherActivity?
        get() = value
}