package com.munch.lib.dialog

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.munch.lib.base.OnCancel
import com.munch.lib.base.OnNext

/**
 * Create by munch1182 on 2021/10/14 09:06.
 */
class HandlerDialog(private val dialog: AlertDialog) : IDialogHandler {

    override fun setOnCancel(onCancel: OnCancel) =
        setOnCancel(dialog.context.getString(android.R.string.cancel), onCancel)

    override fun setOnNext(onNext: OnNext) =
        setOnNext(dialog.context.getString(android.R.string.ok), onNext)

    fun setOnCancel(cancelText: String, onCancel: OnCancel): IDialogHandler {
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancelText) { dialog, _ ->
            onCancel.invoke()
            dialog.dismiss()
        }
        return this
    }

    fun setOnNext(nextText: String, onNext: OnNext): IDialogHandler {
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, nextText) { _, _ -> onNext.invoke() }
        return this
    }

    override fun show() {
        dialog.show()
    }

    override fun dismiss() {
        dialog.dismiss()
    }
}

fun AlertDialog.setOnCancel(onCancel: OnCancel) = HandlerDialog(this).setOnCancel(onCancel)
fun AlertDialog.setOnNext(onNext: OnNext) = HandlerDialog(this).setOnNext(onNext)
fun AlertDialog.setOnCancel(cancelText: String, onCancel: OnCancel) =
    HandlerDialog(this).setOnCancel(cancelText, onCancel)

fun AlertDialog.setOnNext(nextText: String, onNext: OnNext) =
    HandlerDialog(this).setOnNext(nextText, onNext)

fun AlertDialog.withHandler() = HandlerDialog(this)
fun AlertDialog.Builder.withHandler() = HandlerDialog(create())