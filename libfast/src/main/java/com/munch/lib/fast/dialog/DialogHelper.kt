package com.munch.lib.fast.dialog

import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.munch.lib.OnCancel
import com.munch.lib.Priority
import com.munch.lib.notice.INotice

/**
 * Create by munch1182 on 2022/4/2 17:26.
 */
class BottomFragmentNotice(
    private val dialog: BottomSheetDialogFragment,
    private val fm: FragmentManager,
    override val priority: Priority = Priority(0)
) : INotice {
    override fun show() {
        dialog.show(fm, null)
    }

    override fun cancel() {
        dialog.dialog?.cancel()
    }

    override fun onCancel(onCancel: OnCancel?) {
        if (onCancel != null) {
            dialog.dialog?.setOnCancelListener { onCancel.invoke() }
        } else {
            dialog.dialog?.setOnCancelListener(null)
        }
    }

    override val isShowing: Boolean
        get() = dialog.dialog?.isShowing ?: false
}