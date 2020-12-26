package com.munch.lib.test

import android.content.Context
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.munch.lib.helper.setMargin

/**
 * Create by munch1182 on 2020/12/10 15:36.
 */
class TestDialog(private val context: Context) {

    fun simple() = SimpleDialog(context)

    fun bottom() = BottomDialog(context)

    class SimpleDialog(context: Context) {
        private val builder = AlertDialog.Builder(context)
        private var dialog: AlertDialog? = null

        fun setContent(content: String): SimpleDialog {
            builder.setMessage(content)
            return this
        }

        fun setConfirmListener(func: (dialog: AlertDialog) -> Unit): SimpleDialog {
            builder.setPositiveButton(
                android.R.string.ok
            ) { dialog, _ ->
                func(this.dialog!!)
                dialog.dismiss()
            }
            return this
        }

        fun show() {
            dialog =
                builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                    .create()
            dialog!!.show()
        }

        fun cancel() {
            dialog?.cancel()
        }

    }

    class BottomDialog(private val context: Context) {

        private val dialog = BottomSheetDialog(context)
            .apply {
                setContentView(R.layout.layout_bottom_dialog)
            }

        fun addItems(vararg name: String): BottomDialog {
            var rb: RadioButton
            name.forEach {
                rb = RadioButton(context).apply {
                    text = it
                }
                dialog.findViewById<RadioGroup>(R.id.bottom_dialog_rg)?.addView(rb)
                rb.setMargin(0, 8)
            }
            return this
        }

        fun setOnCheckListener(func: (pos: Int) -> Unit): BottomDialog {
            val rg = dialog.findViewById<RadioGroup>(R.id.bottom_dialog_rg)
                ?: return this
            rg.setOnCheckedChangeListener { _, _ ->
                rg.children.forEachIndexed { index, view ->
                    if (view is RadioButton && view.isChecked) {
                        func(index)
                        return@setOnCheckedChangeListener
                    }
                }
                func(-1)
            }
            return this
        }

        fun setConfirmListener(func: (pos: Int) -> Unit): BottomDialog {
            dialog.findViewById<View>(R.id.bottom_dialog_ok)
                ?.setOnClickListener {
                    val rg = dialog.findViewById<RadioGroup>(R.id.bottom_dialog_rg)
                        ?: return@setOnClickListener
                    rg.children.forEachIndexed { index, view ->
                        if (view is RadioButton && view.isChecked) {
                            func(index)
                            dialog.cancel()
                            return@setOnClickListener
                        }
                    }
                    func(-1)
                    dialog.cancel()
                }
            return this
        }

        fun show() {
            dialog.findViewById<View>(R.id.bottom_dialog_cancel)?.setOnClickListener {
                dialog.cancel()
            }
            dialog.show()
        }

        fun cancel() {
            dialog.cancel()
        }

    }
}