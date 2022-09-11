package com.munch.project.one.base

import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.DispatcherActivity
import com.munch.lib.fast.view.SupportActionBar

open class BaseActivity : DispatcherActivity()

fun dispatchDef(showHome: Boolean = true): ActivityDispatch {
    return SupportActionBar(showHome)
}

