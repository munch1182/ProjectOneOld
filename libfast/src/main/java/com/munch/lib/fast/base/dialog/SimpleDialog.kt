package com.munch.lib.fast.base.dialog

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.munch.lib.fast.R
import com.munch.pre.lib.base.listener.ViewIntTagClickListener
import com.munch.pre.lib.extend.ViewHelper

/**
 * Create by munch1182 on 2021/4/2 15:21.
 */
sealed class SimpleDialog {

    class Normal(context: Context) : SimpleDialog() {

        private val db = AlertDialog.Builder(context)

        fun setTitle(title: CharSequence): Normal {
            db.setTitle(title)
            return this
        }

        fun setContent(context: CharSequence): Normal {
            db.setMessage(context)
            return this
        }

        fun setSureClickListener(click: () -> Unit, text: String = "确定"): Normal {
            db.setPositiveButton(text) { _, _ ->
                click.invoke()
                cancel()
            }
            setCancelClickListener()
            return this
        }

        fun setCancelClickListener(click: (() -> Unit)? = null, text: String = "取消"): Normal {
            db.setNegativeButton(text) { _, _ ->
                click?.invoke()
                cancel()
            }
            return this
        }

        override fun getDialog(): AppCompatDialog {
            return db.create()
        }
    }

    class Bottom(private val context: Context) : SimpleDialog() {

        private val clickCallback: ViewIntTagClickListener = object : ViewIntTagClickListener {
            override fun onClick(v: View, index: Int) {
                clickListener?.onClick(v, index)
            }
        }
        private var clickListener: ViewIntTagClickListener? = null
        private val contentView by lazy {
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                setBackgroundColor(Color.WHITE)
            }
        }

        private val bs = BottomSheetDialog(context).apply { setContentView(contentView) }

        fun addItems(vararg name: String): Bottom {
            name.forEachIndexed { index, s ->
                contentView.addView(newItemView(index, s))
            }
            return this
        }

        fun setTitle(title: CharSequence): Bottom {
            val child = contentView.getChildAt(0)
            if (child is TextView) {
                child.text = title
            } else {
                contentView.addView(TextView(context).apply {
                    text = title
                    setPadding(16, 16, 16, 16)
                    textSize = context.resources.getDimension(R.dimen.sp_title)
                }, 0, ViewHelper.newParamsMW())
            }
            return this
        }

        private fun newItemView(index: Int, s: String): View {
            return Button(context).apply {
                text = s
                tag = index
                setOnClickListener(clickCallback)
                layoutParams = ViewHelper.newParamsWW()
            }
        }

        override fun getDialog(): AppCompatDialog {
            return bs
        }

    }

    protected abstract fun getDialog(): AppCompatDialog

    fun show() {
        getDialog().show()
    }

    fun cancel() {
        getDialog().cancel()
    }
}