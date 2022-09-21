package com.munch.lib.fast.view.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.munch.lib.android.AppHelper
import com.munch.lib.android.define.ViewProvider
import com.munch.lib.android.dialog.ChoseDialog
import com.munch.lib.android.dialog.DialogChose
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.dialog.IDialogChose
import com.munch.lib.android.extend.ViewBindViewHelper
import com.munch.lib.android.extend.lazy
import com.munch.lib.fast.R
import com.munch.lib.fast.view.base.ActivityHelper

/**
 * 融合[BottomSheetDialogFragment],[IDialog]和[ActionDialogHelper]
 */
class BottomActionDialog(customViewCreator: DialogViewCreator<DialogActionKey> = DefaultDialogViewCreator()) :
    BottomSheetDialogFragment(), IDialog, ChoseDialog, ViewProvider {

    private val curr = ActivityHelper.curr!!
    private val container by lazy { FrameLayout(curr) }
    private val helper = ActionDialogHelper(customViewCreator)
    private var finChose: DialogChose? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = this.container

    override fun setView(view: View) {
        helper.add(object : DialogAction<DialogActionKey> {
            override val key: DialogActionKey = DialogActionKey.Content
            override val view: View = view
        })
    }

    fun content(view: View): BottomActionDialog {
        setView(view)
        return this
    }

    fun content(string: String): BottomActionDialog {
        helper.add(DialogActionContent(curr, string))
        return this
    }

    override fun show() {
        // 背景为透明, 如果需要圆角需要自行布局实现
        setStyle(DialogFragment.STYLE_NORMAL, R.style.App_Fast_Dialog_Bottom)
        container.addView(helper.getContentView(curr))
        // 必须要在Activity有效时显示
        ActivityHelper.curr?.let { show(it.supportFragmentManager, null) }
    }


    override val chose: IDialogChose?
        get() = finChose

    private fun choseOk() {
        finChose = DialogChose.Ok
    }

    private fun choseCancel() {
        finChose = DialogChose.Cancel
    }

    fun title(s: String): BottomActionDialog {
        helper.add(DialogActionTitle(curr, s))
        return this
    }

    fun cancel(cancel: String = AppHelper.getString(android.R.string.cancel)): BottomActionDialog {
        helper.add(
            DialogActionCancel(curr, cancel).setOnClickListener {
                choseCancel()
                dismiss()
            }
        )
        return this
    }

    fun ok(ok: String = AppHelper.getString(android.R.string.ok)): BottomActionDialog {
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
    ): BottomActionDialog {
        return object : ViewBindViewHelper<VB, BottomActionDialog>(this, context) {}.set(set)
    }
}