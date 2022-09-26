package com.munch.lib.fast.view.dialog

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.munch.lib.android.AppHelper
import com.munch.lib.android.define.ViewProvider
import com.munch.lib.android.dialog.ChoseDialogWrapper
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.dialog.toDialog
import com.munch.lib.android.extend.ViewBindTargetHelper
import com.munch.lib.fast.R
import com.munch.lib.fast.view.base.ActivityHelper

/**
 * 使用[BottomSheetDialog]实现IDialog和ChoseDialogWrapper
 */
class BottomAction2Dialog(customViewCreator: DialogViewCreator<DialogActionKey> = DefaultDialogViewCreator()) :
    IDialog, ChoseDialogWrapper(), ViewProvider {

    private val curr = ActivityHelper.curr!!
    private val helper = ActionDialogHelper(customViewCreator)

    override fun crateDialog(): IDialog {
        return BottomSheetDialog(curr, R.style.App_Fast_Dialog_Bottom)
            .apply { setContentView(helper.getContentView(curr)) }
            .toDialog()
    }


    override fun setView(view: View) {
        helper.add(object : DialogAction<DialogActionKey> {
            override val key: DialogActionKey = DialogActionKey.Content
            override val view: View = view
        })
    }

    fun content(view: View): BottomAction2Dialog {
        setView(view)
        return this
    }

    fun content(string: String): BottomAction2Dialog {
        helper.add(DialogActionContent(curr, string))
        return this
    }

    fun background(@ColorInt color: Int): BottomAction2Dialog {
        return background(LinearLayout(curr).apply {
            setBackgroundColor(color)
            orientation = LinearLayout.VERTICAL
        })
    }

    fun background(view: View): BottomAction2Dialog {
        helper.add(object : DialogAction<DialogActionKey> {
            override val key: DialogActionKey = DialogActionKey.Background
            override val view: View = view
        })
        return this
    }

    fun title(s: String): BottomAction2Dialog {
        helper.add(DialogActionTitle(curr, s))
        return this
    }

    fun cancel(cancel: String = AppHelper.getString(android.R.string.cancel)): BottomAction2Dialog {
        helper.add(
            DialogActionCancel(curr, cancel).setOnClickListener {
                choseCancel()
                dismiss()
            }
        )
        return this
    }

    fun ok(ok: String = AppHelper.getString(android.R.string.ok)): BottomAction2Dialog {
        helper.add(
            DialogActionOk(curr, ok).setOnClickListener {
                choseOk()
                dismiss()
            }
        )
        return this
    }

    inline fun <reified VB : ViewBinding> bind(
        context: Context = ActivityHelper.curr!!,
        noinline set: (VB.() -> Unit)? = null
    ): BottomAction2Dialog {
        return object : ViewBindTargetHelper<VB, BottomAction2Dialog>(this, context) {}.set(set)
    }
}