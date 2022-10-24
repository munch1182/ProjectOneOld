package com.munch.lib.fast.view.dialog

import android.content.Context
import android.view.View
import androidx.viewbinding.ViewBinding
import com.munch.lib.android.AppHelper
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.extend.ViewBindViewHelper

/**
 * Create by munch1182 on 2022/10/24 11:16.
 */
fun BottomAction2Dialog.titleStr(s: String): BottomAction2Dialog {
    helper.add(DialogActionTitle(curr, s))
    return this
}

fun BottomAction2Dialog.cancelStr(cancel: String = AppHelper.getString(android.R.string.cancel)): BottomAction2Dialog {
    helper.add(
        DialogActionCancel(curr, cancel)
            .setOnClickListener {
                choseCancel()
                dismiss()
            }
    )
    return this
}

fun BottomAction2Dialog.okStr(ok: String = AppHelper.getString(android.R.string.ok)): BottomAction2Dialog {
    helper.add(
        DialogActionOk(curr, ok)
            .setOnClickListener {
                choseOk()
                dismiss()
            }
    )
    return this
}

fun BottomAction2Dialog.contentStr(string: String): BottomAction2Dialog {
    helper.add(DialogActionContent(curr, string))
    return this
}

/**
 * 使用view作为dialog显示并在dialog消失时回调view
 *
 * @see ViewSetter 联动此类使用
 */
fun <V : View> BottomAction2Dialog.view(view: V): ViewSetter<V, BottomAction2Dialog> {
    content(view)
    return ViewSetter(this, view)
}

/**
 * 使用ViewBind作为dialog显示并在dialog消失时回调ViewBind
 *
 * @see ViewBindSetter 联动此类使用
 */
inline fun <reified VB : ViewBinding> BottomAction2Dialog.view(context: Context): ViewBindSetter<VB, BottomAction2Dialog> {
    val vb = object : ViewBindViewHelper<VB>(context) {}.vb
    content(vb.root)
    return ViewBindSetter(this, vb)
}

//<editor-fold desc="define">
/**
 * 将[IDialog.onShow]和[IDialog.onDismiss]的回调的参数改为指定类型
 */
interface OnDialogContent<CONTENT, DIALOG : IDialog> {
    fun onShow(content: CONTENT.(dialog: DIALOG) -> Unit): DIALOG
    fun onDismiss(content: CONTENT.(dialog: DIALOG) -> Unit): DIALOG
    fun onShowAnd(content: CONTENT.(dialog: DIALOG) -> Unit): OnDialogContent<CONTENT, DIALOG> {
        onShow(content)
        return this
    }
    fun onDismissAnd(content: CONTENT.(dialog: DIALOG) -> Unit): OnDialogContent<CONTENT, DIALOG> {
        onDismiss(content)
        return this
    }
}

/**
 * 将[IDialog.onShow]和[IDialog.onDismiss]的回调的参数改为[View]
 */
class ViewSetter<V : View, DIALOG : IDialog>(private val dialog: DIALOG, private val view: V) :
    OnDialogContent<V, DIALOG> {
    override fun onShow(content: V.(dialog: DIALOG) -> Unit): DIALOG {
        dialog.onShow<DIALOG> { content.invoke(view, dialog) }
        return dialog
    }
    override fun onDismiss(content: V.(dialog: DIALOG) -> Unit): DIALOG {
        dialog.onDismiss<DIALOG> { content.invoke(view, dialog) }
        return dialog
    }
}

/**
 * 将[IDialog.onShow]和[IDialog.onDismiss]的回调的参数改为[ViewBinding]
 */
class ViewBindSetter<VB : ViewBinding, DIALOG : IDialog>(
    private val dialog: DIALOG,
    private val vb: VB
) : OnDialogContent<VB, DIALOG> {
    override fun onShow(content: VB.(dialog: DIALOG) -> Unit): DIALOG {
        dialog.onShow<DIALOG> { content.invoke(vb, dialog) }
        return dialog
    }
    override fun onDismiss(content: VB.(dialog: DIALOG) -> Unit): DIALOG {
        dialog.onDismiss<DIALOG> { content.invoke(vb, dialog) }
        return dialog
    }
}
//</editor-fold>