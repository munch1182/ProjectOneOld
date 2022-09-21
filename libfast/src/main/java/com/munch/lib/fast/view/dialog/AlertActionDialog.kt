package com.munch.lib.fast.view.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.dialog.toDialog
import com.munch.lib.android.extend.dp2Px2Int
import com.munch.lib.android.extend.newMWLP
import com.munch.lib.android.extend.newWWLP
import com.munch.lib.fast.view.base.ActivityHelper

/**
 * Create by munch on 2022/9/21 20:18.
 */

/**
 * 仅显示提醒的dialog, 使用的是[AlertDialog], 且必定会调用[ok]
 */
class MessageDialog : TextActionDialog() {

    override val context: Context = ActivityHelper.curr!! // 必须在Activity显示后未关闭前创建和显示dialog

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

/**
 * 默认样式的布局方式
 */
class DefaultDialogViewCreator : DialogViewCreator<DialogActionKey> {
    override fun create(
        context: Context,
        map: Map<DialogActionKey, DialogAction<DialogActionKey>?>
    ): View {
        val dp16 = 16.dp2Px2Int()
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        map[DialogActionKey.Title]?.let {
            container.addView(it.view.apply { setPadding(dp16) }, newMWLP)
        }
        map[DialogActionKey.Content]?.let {
            if (!map.containsKey(DialogActionKey.Title)) {
                it.view.setPadding(dp16, dp16, dp16, 0)
            } else {
                it.view.setPadding(dp16, 0, dp16, 0)
            }
            container.addView(it.view, newMWLP)
        }
        val choseContainer = LinearLayout(context).apply { setPadding(dp16) }
        map[DialogActionKey.Cancel]?.let {
            choseContainer.addView(
                it.view.apply {
                    setPadding(dp16, dp16 / 2, dp16, dp16 / 2)
                }, newWWLP
            )
        }
        map[DialogActionKey.Ok]?.let {
            choseContainer.addView(
                it.view.apply {
                    setPadding(dp16, dp16 / 2, dp16, dp16 / 2)
                }, newWWLP
            )
        }
        if (choseContainer.childCount > 0) {
            choseContainer.gravity = Gravity.END
            container.addView(choseContainer, newMWLP)
        }
        return container
    }
}