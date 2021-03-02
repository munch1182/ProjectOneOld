package com.munch.project.launcher.base

import android.content.Context
import androidx.appcompat.app.AlertDialog

/**
 * dialog统一类，主要是为了统一样式
 *
 * Create by munch1182 on 2021/3/2 14:08.
 */
sealed class DialogHelper {

    fun center(context: Context) = Center(context)

    class Center(context: Context) : DialogHelper() {

        private val dialogBuilder = AlertDialog.Builder(context)
        private lateinit var dialog: AlertDialog

        fun set(builder: AlertDialog.Builder.() -> Unit): Center {
            dialogBuilder.setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            builder.invoke(dialogBuilder)
            return this
        }

        override fun show() {
            super.show()
            dialog = dialogBuilder.create()
            dialog.show()
        }

        override fun dismiss() {
            super.dismiss()
            dialog.cancel()
        }
    }

    open fun show() {}

    open fun dismiss() {}
}