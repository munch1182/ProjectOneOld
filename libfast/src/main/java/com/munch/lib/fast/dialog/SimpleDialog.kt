package com.munch.lib.fast.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog

/**
 * Create by munch1182 on 2021/10/16 11:11.
 */
class SimpleDialog(context: Context, @StyleRes themeResId: Int = 0) :
    AlertDialog(context, themeResId) {

    fun setOnSureListener(sure: (dialog: DialogInterface) -> Unit): SimpleDialog {
        setButton(
            DialogInterface.BUTTON_POSITIVE,
            context.getString(android.R.string.ok)
        ) { dialog, _ ->
            sure.invoke(dialog)
        }
        return this
    }

    override fun show() {
        show(true)
    }

    fun show(simple: Boolean = true) {
        if (simple) {
            setTitle("提示")
            setButton(
                DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel)
            ) { dialog, _ -> dialog.cancel() }
        }
        super.show()
    }

    fun setContent(message: CharSequence?): SimpleDialog {
        super.setMessage(message)
        return this
    }
}