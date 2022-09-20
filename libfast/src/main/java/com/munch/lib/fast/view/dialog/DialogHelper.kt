package com.munch.lib.fast.view.dialog

import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.dialog.toDialog
import com.munch.lib.fast.view.base.ActivityHelper

/**
 * Create by munch1182 on 2022/9/20 11:48.
 */
object DialogHelper {

    fun message() = MessageDialog()

    /**
     * 仅提示消息的dialog
     */
    fun message(s: String) = message().message(s)

    /**
     * 从底部弹窗dialog
     */
    fun bottom() = BottomDialog()

    fun view(view: DialogAction<DialogActionKey>) = bottom().view(view)

}

/**
 * 融合[BottomSheetDialogFragment],[IDialog]和[ActionDialogHelper]
 */
class BottomDialog(customViewCreator: DialogViewCreator<DialogActionKey> = DefaultDialogViewCreator()) :
    BottomSheetDialogFragment(), IDialog {

    private val helper = ActionDialogHelper(customViewCreator)

    fun view(view: DialogAction<DialogActionKey>): BottomDialog {
        helper.add(view)
        return this
    }

    override fun show() {
        ActivityHelper.curr?.let { show(it.supportFragmentManager, null) }
    }

    override fun cancel() {
        dismiss()
    }
}

/**
 * 仅显示提醒的dialog, 使用的是[AlertDialog], 且必定会调用[ok]
 */
class MessageDialog : ActionDialog() {

    override fun cancel(cancel: String) = ok(cancel)

    override fun crateDialog(): IDialog {
        if (DialogActionKey.Ok !in helper) {
            ok(context.getString(android.R.string.ok))
        }
        return AlertDialog.Builder(context)
            .setView(helper.getContentView(context))
            .create().toDialog()
    }
}
