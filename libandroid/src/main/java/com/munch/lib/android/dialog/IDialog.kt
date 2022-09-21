package com.munch.lib.android.dialog

import android.content.Context
import androidx.activity.ComponentDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.android.extend.impInMain
import com.munch.lib.android.extend.to
import kotlinx.coroutines.runBlocking

/**
 * 统一Dialog的逻辑
 *
 * 如果要实现Dialog, 则必须实现其[LifecycleOwner]
 */
interface IDialog : LifecycleOwner {

    fun show()

    fun cancel()

    fun <DIALOG : IDialog> onShow(l: DialogShowListener<DIALOG>?): IDialog {
        impInMain { lifecycle.addObserver(Lifecycle2Listener(this.to(), show = l, null)) }
        return this
    }

    fun <DIALOG : IDialog> onCancel(l: DialogCancelListener<DIALOG>?): IDialog {
        impInMain { lifecycle.addObserver(Lifecycle2Listener(this.to(), null, cancel = l)) }
        return this
    }
}

fun interface DialogShowListener<DIALOG : IDialog> {
    fun onShow(dialog: DIALOG)
}

fun interface DialogCancelListener<DIALOG : IDialog> {
    fun onCancel(dialog: DIALOG)
}

/**
 * 将[DefaultLifecycleObserver]转为[DialogShowListener]或者[DialogCancelListener]
 */
class Lifecycle2Listener<DIALOG : IDialog>(
    private val dialog: DIALOG,
    private val show: DialogShowListener<DIALOG>?,
    private val cancel: DialogCancelListener<DIALOG>?
) : DefaultLifecycleObserver {

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        show?.onShow(dialog)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        cancel?.onCancel(dialog)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        owner.lifecycle.removeObserver(this)
    }
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
