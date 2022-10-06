package com.munch.lib.android.dialog

import androidx.activity.ComponentDialog
import androidx.lifecycle.Lifecycle
import com.munch.lib.android.extend.impInMain
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Create by munch on 2022/9/21 19:42.
 */
//<editor-fold desc="extend">
/**
 * 将[ComponentDialog]包装为[IDialog]
 */
open class DialogWrapper(private val dialog: ComponentDialog) : IDialog {
    override fun show() {
        dialog.show()
    }

    override fun dismiss() {
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

//<editor-fold desc="extend">
/**
 * 显示[ChoseDialog]
 * 并当[ChoseDialog]消失时, 返回其选择的结果
 */
suspend fun ChoseDialog.showThenReturnChose(dm: IDialogManager? = null): IDialogChose? =
    suspendCancellableCoroutine {
        impInMain {
            val dialog = onDismiss<ChoseDialog> { d -> it.resume(d.chose) }
            if (dm != null) {
                dm.add(dialog).show()
            } else {
                dialog.show()
            }
        }
    }

abstract class ChoseDialogWrapper : ChoseDialog {

    protected open val dialog by lazy { crateDialog() }
    private var finChose: DialogChose? = null

    override val chose: IDialogChose?
        get() = finChose

    protected fun choseOk() {
        finChose = DialogChose.Ok
    }

    protected fun choseCancel() {
        finChose = DialogChose.Cancel
    }

    /**
     * 拓展一个IDialog的对象(可能非ChoseDialog]), 使其实现ChoseDialog
     */
    protected abstract fun crateDialog(): IDialog

    override fun show() {
        dialog.show()
    }

    override fun dismiss() {
        dialog.dismiss()
    }

    override fun getLifecycle(): Lifecycle = dialog.lifecycle
}
//</editor-fold>

//<editor-fold desc="extend">
/**
 * 将[ComponentDialog]加入到[IDialogManager]队列中, 由队列管理显示
 */
@Suppress("NOTHING_TO_INLINE")
inline fun ComponentDialog.offer(m: IDialogManager) = m.add(this.toDialog())

/**
 * 将[IDialog]加入到[IDialogManager]队列中, 由队列管理显示
 */
@Suppress("NOTHING_TO_INLINE")
inline fun IDialog.offer(m: IDialogManager) = m.add(this)

/**
 * 将[androidx.appcompat.app.AlertDialog]加入到[IDialogManager]队列中, 由队列管理显示
 */
@Suppress("NOTHING_TO_INLINE")
inline fun androidx.appcompat.app.AlertDialog.Builder.offer(m: IDialogManager): IDialogManager {
    impInMain { create().offer(m) }
    return m
}
//</editor-fold>