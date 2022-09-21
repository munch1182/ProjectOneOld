package com.munch.lib.fast.view.dialog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.munch.lib.android.define.ViewProvider
import com.munch.lib.android.dialog.*
import com.munch.lib.android.extend.*
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

    inline fun <reified VB : ViewBinding> bottom(
        context: Context = ActivityHelper.curr!!,
        noinline set: (VB.() -> Unit)? = null
    ) = BottomDialog().bind(context, set)
}

/**
 * 融合[BottomSheetDialogFragment],[IDialog]和[ActionDialogHelper]
 */
class BottomDialog(customViewCreator: DialogViewCreator<DialogActionKey> = DefaultDialogViewCreator()) :
    BottomSheetDialogFragment(), IDialog, ChoseDialog, ViewProvider {

    private val curr = ActivityHelper.curr!!
    private val container by lazy { FrameLayout(curr) }
    private val helper = ActionDialogHelper(customViewCreator)

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

    override fun show() {
        container.addView(helper.getContentView(curr))
        ActivityHelper.curr?.let { show(it.supportFragmentManager, null) }
    }

    override fun cancel() {
        dismiss()
    }

    private var finChose: DialogChose? = null

    override val chose: IDialogChose?
        get() = finChose

    fun choseOk() {
        finChose = DialogChose.Ok
    }

    fun choseCancel() {
        finChose = DialogChose.Cancel
    }

    fun message(s: String): BottomDialog {
        helper.add(DialogActionContent(curr, s))
        return this
    }

    fun title(s: String): BottomDialog {
        helper.add(DialogActionTitle(curr, s))
        return this
    }

    fun cancel(cancel: String): BottomDialog {
        helper.add(
            DialogActionCancel(curr, cancel)
                .setOnClickListener {
                    choseCancel()
                    cancel()
                }
        )
        return this
    }

    fun ok(ok: String): BottomDialog {
        helper.add(
            DialogActionOk(curr, ok)
                .setOnClickListener {
                    choseOk()
                    cancel()
                }
        )
        return this
    }

    inline fun <reified VB : ViewBinding> bind(
        context: Context = ActivityHelper.curr!!,
        noinline set: (VB.() -> Unit)? = null
    ): BottomDialog {
        return object : ViewBindViewHelper<VB, BottomDialog>(this, context) {}.set(set)
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