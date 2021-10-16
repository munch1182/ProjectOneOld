package com.munch.lib.fast.dialog

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import com.munch.lib.base.hideSoftInput
import com.munch.lib.base.showSoftInput
import com.munch.lib.fast.databinding.LayoutSimpleEditDialogBinding

/**
 * Create by munch1182 on 2021/10/16 11:11.
 */
class SimpleEditDialog(context: Context, @StyleRes themeResId: Int = 0) :
    AlertDialog(context, themeResId) {

    private val binding by lazy { LayoutSimpleEditDialogBinding.inflate(layoutInflater) }

    private val content: String
        get() = binding.simpleEditEt.text.toString().trim()
    private var onText: OnText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        binding.simpleEditCancel.setOnClickListener { cancel() }
        binding.simpleEditSure.setOnClickListener {
            onText?.invoke(this, content)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            binding.simpleEditEt.showSoftInput()
        } else {
            binding.simpleEditEt.hideSoftInput()
        }
    }

    fun setOnTextListener(onText: OnText): SimpleEditDialog {
        this.onText = onText
        return this
    }

    override fun setTitle(title: CharSequence?) {
        binding.simpleEditTitle.text = title
    }

    override fun setMessage(message: CharSequence?) {
        super.setMessage(message)
        binding.simpleEditTitle.text = message
    }
}

typealias OnText = (dialog: SimpleEditDialog, text: String) -> Unit