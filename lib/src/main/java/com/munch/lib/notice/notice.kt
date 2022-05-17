package com.munch.lib.notice

import androidx.appcompat.app.AlertDialog
import com.munch.lib.OnCancel
import com.munch.lib.Priority


/**
 * Create by munch1182 on 2022/4/22 16:38.
 */
interface INotice {

    fun show()

    fun cancel()

    val priority: Priority
        get() = Priority(0)

    fun onCancel(onCancel: OnCancel? = null)

    val isShowing: Boolean
}

class DialogNotice(
    private val dialog: AlertDialog,
    override val priority: Priority = Priority(0)
) : INotice {

    override fun show() {
        dialog.show()
    }

    override fun cancel() {
        dialog.cancel()
    }

    override fun onCancel(onCancel: OnCancel?) {
        if (onCancel != null) {
            dialog.setOnCancelListener { onCancel.invoke() }
        } else {
            dialog.setOnCancelListener(null)
        }
    }

    override val isShowing: Boolean
        get() = dialog.isShowing
}

@Suppress("NOTHING_TO_INLINE")
inline fun AlertDialog.Builder.toNotice(priority: Priority = Priority(0)) =
    DialogNotice(this.create(), priority)