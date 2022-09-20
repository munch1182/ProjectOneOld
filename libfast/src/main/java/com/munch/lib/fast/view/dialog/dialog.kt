package com.munch.lib.fast.view.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.munch.lib.android.dialog.ChoseDialogWrapper
import com.munch.lib.android.extend.clickEffect
import com.munch.lib.android.extend.dp2Px2Int
import com.munch.lib.android.extend.newMWLP
import com.munch.lib.android.extend.newWWLP
import com.munch.lib.fast.R
import com.munch.lib.fast.view.base.ActivityHelper

/**
 * 使用[ActionDialogHelper]来收集设置的[DialogAction], 并最后在[crateDialog]中由[helper]对所有的[DialogAction]进行布局并返回最终的布局view
 *
 * 此类是通用的[DialogActionKey]组成, 布局方式由构造传入
 *
 * Create by munch1182 on 2022/9/20 11:48.
 */
abstract class ActionDialog(
    customViewCreator: DialogViewCreator<DialogActionKey> = DefaultDialogViewCreator()
) : ChoseDialogWrapper() {

    protected open val helper = ActionDialogHelper(customViewCreator)
    protected open val context = ActivityHelper.curr!!

    open fun message(s: String): ActionDialog {
        helper.add(DialogActionContent(context, s))
        return this
    }

    open fun title(s: String): ActionDialog {
        helper.add(DialogActionTitle(context, s))
        return this
    }

    open fun cancel(cancel: String): ActionDialog {
        helper.add(
            DialogActionCancel(context, cancel)
                .setOnClickListener {
                    choseCancel()
                    cancel()
                }
        )
        return this
    }

    open fun ok(ok: String): ActionDialog {
        helper.add(
            DialogActionOk(context, ok)
                .setOnClickListener {
                    choseOk()
                    cancel()
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