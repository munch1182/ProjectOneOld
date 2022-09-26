package com.munch.lib.android.dialog

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.android.extend.impInMain
import com.munch.lib.android.extend.to

/**
 * 统一Dialog的逻辑
 *
 * 如果要实现Dialog, 则必须实现其[LifecycleOwner]
 */
interface IDialog : LifecycleOwner {

    fun show()

    fun dismiss()

    /**
     * 当dialog显示时的回调
     */
    fun <DIALOG : IDialog> onShow(l: DialogShowListener<DIALOG>?): DIALOG {
        impInMain { lifecycle.addObserver(Lifecycle2Listener(this.to(), show = l, null)) }
        return this.to()
    }

    /**
     * 当dialog取消显示后的回调
     */
    fun <DIALOG : IDialog> onDismiss(l: DialogDismissListener<DIALOG>?): DIALOG {
        impInMain { lifecycle.addObserver(Lifecycle2Listener(this.to(), null, dismiss = l)) }
        return this.to()
    }
}

fun interface DialogShowListener<DIALOG : IDialog> {
    fun onShow(dialog: DIALOG)
}

fun interface DialogDismissListener<DIALOG : IDialog> {
    fun onDismiss(dialog: DIALOG)
}

/**
 * 将[DefaultLifecycleObserver]转为[DialogShowListener]或者[DialogDismissListener]
 */
class Lifecycle2Listener<DIALOG : IDialog>(
    private val dialog: DIALOG,
    private val show: DialogShowListener<DIALOG>?,
    private val dismiss: DialogDismissListener<DIALOG>?
) : DefaultLifecycleObserver {

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        show?.onShow(dialog)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        dismiss?.onDismiss(dialog)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        owner.lifecycle.removeObserver(this)
    }
}
