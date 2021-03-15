@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.test

import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.munch.lib.helper.clickItem
import com.munch.lib.helper.setMargin

/**
 * Create by munch1182 on 2020/12/10 15:36.
 */
class TestDialog(private val context: Context) {

    companion object {

        fun simple(context: Context) = TestDialog(context).simple()
        fun bottom(context: Context) = TestDialog(context).bottom()
    }

    fun simple() = SimpleDialog(context)

    fun bottom() = BottomDialog(context)

    class SimpleDialog(context: Context) {
        private val builder = AlertDialog.Builder(context)
        private var dialog: AlertDialog? = null
        private var canceled = false

        fun setContent(content: String): SimpleDialog {
            builder.setMessage(content)
            return this
        }

        fun setConfirmListener(func: (dialog: AlertDialog) -> Unit): SimpleDialog {
            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                func(this.dialog!!)
                dialog.cancel()
            }
            return this
        }

        fun setCancelListener(func: (dialog: AlertDialog) -> Unit): SimpleDialog {
            builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                func(this.dialog!!)
                dialog.cancel()
            }
            canceled = true
            return this
        }

        fun show() {
            if (!canceled) {
                builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            }
            dialog = builder.create()
            dialog!!.show()
        }

        fun cancel() {
            dialog?.cancel()
        }

        fun setTitle(s: String): SimpleDialog {
            dialog?.setTitle(s)
            return this
        }

    }

    class BottomDialog(private val context: Context) {

        private val dialog = BottomSheetDialog(context)
            .apply {
                setContentView(R.layout.layout_bottom_dialog)

            }
        private val container by lazy { dialog.findViewById<ViewGroup>(R.id.bottom_dialog_container) }


        fun addItems(vararg name: String): BottomDialog {
            name.forEach {
                container?.addView(Button(context).apply {
                    text = it
                    setMargin(0, 8)
                })
            }
            container?.addView(Button(context).apply {
                text = "取消"
                setMargin(0, 16, 0, 8)
                setOnClickListener {
                    cancel()
                }
            })
            return this
        }


        fun setOnClickListener(func: (dialog: BottomDialog, pos: Int) -> Unit): BottomDialog {
            container?.clickItem({
                val i = it.tag as? Int? ?: return@clickItem
                if (i == container!!.childCount - 1) {
                    cancel()
                } else {
                    func(this, i)
                }
            })
            return this
        }

        fun show() {
            dialog.show()
        }

        fun cancel() {
            dialog.cancel()
        }

    }
}