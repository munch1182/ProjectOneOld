package com.munch.lib.fast.view.base

import android.content.Context
import android.view.View
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.extend.SealedClassToStringByName

/**
 * 使用模块来构建Dialog
 *
 * 则作为基础的Dialog只需要负责布局即可
 */
interface ModuleDialog : IDialog {

    fun addModule(module: DialogModule, view: DialogModuleView): ModuleDialog
}

sealed class DialogModule : SealedClassToStringByName() {
    object Title : DialogModule()
    object Content : DialogModule()
    object Cancel : DialogModule()
    object Sure : DialogModule()
}

interface DialogModuleView {
    fun onCreate(context: Context): View
    fun onShow() {}
}