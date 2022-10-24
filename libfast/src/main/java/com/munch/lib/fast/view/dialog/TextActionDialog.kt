package com.munch.lib.fast.view.dialog

import android.content.Context
import android.view.View
import android.widget.TextView
import com.munch.lib.android.AppHelper
import com.munch.lib.android.dialog.ChoseDialogWrapper
import com.munch.lib.android.extend.clickEffect
import com.munch.lib.android.extend.dp2Px2Int
import com.munch.lib.fast.R

/**
 * 使用[ActionDialogHelper]来收集设置的[DialogAction], 并最后在[crateDialog]中由[helper]对所有的[DialogAction]进行布局并返回最终的布局view
 *
 * 此类是通用的[DialogActionKey]组成, 布局方式由构造传入
 *
 * @see ActionDialogHelper
 * @see DialogAction
 *
 * Create by munch1182 on 2022/9/20 11:48.
 */
abstract class TextActionDialog(
    customViewCreator: DialogViewCreator<DialogActionKey> = DefaultDialogViewCreator()
) : ChoseDialogWrapper() {

    protected open val helper = ActionDialogHelper(customViewCreator)
    protected abstract val context: Context

    open fun message(s: String): TextActionDialog {
        helper.add(DialogActionContent(context, s))
        return this
    }

    open fun title(s: String): TextActionDialog {
        helper.add(DialogActionTitle(context, s))
        return this
    }

    open fun cancel(cancel: String = AppHelper.getString(android.R.string.cancel)): TextActionDialog {
        helper.add(
            DialogActionCancel(context, cancel)
                .setOnClickListener {
                    choseCancelOpt()
                    dismiss()
                }
        )
        return this
    }

    open fun ok(ok: String = AppHelper.getString(android.R.string.ok)): TextActionDialog {
        helper.add(
            DialogActionOk(context, ok)
                .setOnClickListener {
                    choseOkOpt()
                    dismiss()
                }
        )
        return this
    }
}

//<editor-fold desc="DialogAction">
/**
 * 默认的由TextView组成的[DialogActionKey.Content]
 */
class DialogActionContent(context: Context, str: String) : DialogAction<DialogActionKey> {
    override val key: DialogActionKey = DialogActionKey.Content
    override val view: View = TextView(context, null, R.attr.fastAttrText).apply { text = str }
}

/**
 * 默认的由TextView组成的[DialogActionKey.Title]
 */
class DialogActionTitle(context: Context, str: String) : DialogAction<DialogActionKey> {
    override val key: DialogActionKey = DialogActionKey.Title
    override val view: View = TextView(context, null, R.attr.fastAttrTextTitle).apply { text = str }
}

/**
 * 默认的由TextView组成的[DialogActionKey.Ok]
 */
class DialogActionOk(context: Context, str: String) : DialogAction<DialogActionKey> {
    override val key: DialogActionKey = DialogActionKey.Ok
    override val view: View = TextView(context, null, R.attr.fastAttrTextTitle).apply {
        val dp16 = 16.dp2Px2Int()
        setPadding(dp16, dp16 / 2, dp16, dp16 / 2)
        clickEffect()
        text = str
    }
}

/**
 * 默认的由TextView组成的[DialogActionKey.Cancel]
 */
class DialogActionCancel(context: Context, str: String) : DialogAction<DialogActionKey> {
    override val key: DialogActionKey = DialogActionKey.Cancel
    override val view: View = TextView(context, null, R.attr.fastAttrTextTitle).apply {
        val dp16 = 16.dp2Px2Int()
        setPadding(dp16, dp16 / 2, dp16, dp16 / 2)
        clickEffect()
        text = str
    }
}
//</editor-fold>