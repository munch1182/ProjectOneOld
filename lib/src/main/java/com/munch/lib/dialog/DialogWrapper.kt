package com.munch.lib.dialog

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.munch.lib.helper.ActivityHelper

/**
 * Create by munch1182 on 2022/4/22 16:38.
 */
interface DialogWrapper {

    fun show()

    fun cancel()

    fun setOnCancelListener(listener: DialogInterface.OnCancelListener): DialogWrapper

    companion object {
        const val BTN_CONFIRM = DialogInterface.BUTTON_POSITIVE
        const val BTN_CANCEL = DialogInterface.BUTTON_NEGATIVE
        const val BTN_NON = 100
    }
}

class AlertDialogWrapper(private val dialog: AlertDialog) : DialogWrapper {

    override fun show() {
        dialog.show()
    }

    override fun cancel() {
        dialog.cancel()
    }

    override fun setOnCancelListener(listener: DialogInterface.OnCancelListener): AlertDialogWrapper {
        dialog.setOnCancelListener(listener)
        return this
    }

}

class DialogFragmentWrapper(private val dialog: DialogFragment) : DialogWrapper {

    override fun show() {
        ActivityHelper.currCreate?.let { it as? FragmentActivity }?.let {
            dialog.show(it.supportFragmentManager, null)
        }
    }

    override fun cancel() {
        dialog.dismissAllowingStateLoss()
    }

    override fun setOnCancelListener(listener: DialogInterface.OnCancelListener): DialogFragmentWrapper {
        dialog.requireDialog().setOnCancelListener(listener)
        return this
    }
}