package com.munch.project.one.base

import com.munch.lib.fast.view.dispatch.*

open class BaseActivity : DispatcherActivity()

fun dispatchDef(dialog: IConfigDialog = SupportConfigDialog()): ActivityDispatch {
    return SupportActionBar() + dialog
}

