package com.munch.lib.android.dialog

import android.content.Context
import androidx.activity.ComponentDialog
import androidx.lifecycle.LifecycleOwner

/**
 * 统一Dialog的逻辑
 *
 * 如果要实现Dialog, 则必须实现其[LifecycleOwner]
 */
interface IDialog : LifecycleOwner {

    fun show()

    fun cancel()
}

/**
 * 用于创建一个Dialog
 */
typealias DialogCreator = (Context) -> IDialog

//<editor-fold desc="extend">
/**
 * 将[ComponentDialog]包装为[IDialog]
 */
open class DialogWrapper(private val dialog: ComponentDialog) : IDialog {
    override fun show() {
        dialog.show()
    }

    override fun cancel() {
        dialog.cancel()
    }

    override fun getLifecycle() = dialog.lifecycle
}

/**
 * 将[ComponentDialog]转为[IDialog]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun ComponentDialog.toDialog(): IDialog = DialogWrapper(this)
//</editor-fold>
