package com.munch.lib.fast.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import com.munch.lib.base.*
import com.munch.lib.dialog.IViewDialog

/**
 * Create by munch1182 on 2021/10/16 11:11.
 */
class SimpleDialog(context: Context, @StyleRes themeResId: Int = 0) :
    AlertDialog(context, themeResId), IViewDialog {

    private var onNext: OnNext? = null
    private var onCancel: OnCancel? = null

    fun setOnSureListener(
        sureStr: String = context.getString(android.R.string.ok),
        sure: (dialog: DialogInterface) -> Unit
    ): SimpleDialog {
        setButton(
            DialogInterface.BUTTON_POSITIVE,
            sureStr
        ) { dialog, _ ->
            onNext?.invoke()
            sure.invoke(dialog)
        }
        onNext = null
        return this
    }

    override fun setOnCancel(onCancel: OnCancel): SimpleDialog {
        this.onCancel = onCancel
        return this
    }

    override fun setOnNext(onNext: OnNext): SimpleDialog {
        this.onNext = onNext
        return this
    }

    override fun setContentView(view: View) {
        super.setContentView(FrameLayout(context).apply {
            addView(view, ViewHelper.newWWLayoutParams().toFrame().apply {
                gravity = Gravity.CENTER
                setPadding(context.dp2Px(16f).toInt())
                setBackgroundColor(Color.parseColor("#66000000"))
            })
        })
    }

    override fun show() {
        show(true)
    }

    fun show(simple: Boolean = true) {
        if (simple) {
            setTitle("提示")
            setButton(
                DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel)
            ) { dialog, _ ->
                onCancel?.invoke()
                dialog.cancel()
            }
        }
        super.show()
    }

    fun setContent(message: CharSequence?): SimpleDialog {
        super.setMessage(message)
        return this
    }

    fun setName(title: CharSequence?): SimpleDialog {
        super.setTitle(title)
        return this
    }
}