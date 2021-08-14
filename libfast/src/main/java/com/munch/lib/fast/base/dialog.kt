package com.munch.lib.fast.base

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.munch.lib.fast.R

/**
 * Create by munch1182 on 2021/8/14 9:06.
 */

fun <ACT : BaseBigTextTitleActivity> ACT.newBottomDialog(
    initView: ((LinearLayout) -> Unit)? = null,
): BottomSheetDialog {
    return commonDialog(this).apply {
        setContentView(R.layout.layout_bottom_sheel_dialog)
        findViewById<LinearLayout>(R.id.dialog_container)?.apply {
            initView?.invoke(this)
            //添加通用设置
            addView(newCheck(this@newBottomDialog))
        }
        findViewById<TextView>(R.id.dialog_title)?.text =
            this@newBottomDialog::class.java.simpleName
    }
}

private fun commonDialog(context: Context) =
    BottomSheetDialog(context, R.style.AppTheme_Dialog_Trans)

private fun <ACT : BaseBigTextTitleActivity> newCheck(context: ACT): View {
    return View.inflate(context, R.layout.layout_item_checkbox, null).apply {
        val cb = findViewById<CheckBox>(R.id.item_cb_view)
        //设置不可点击，将点击事件交给Parent处理
        cb.isClickable = false
        //去掉toggle的水波纹效果
        cb.setBackgroundColor(Color.TRANSPARENT)
        if (DataHelper.firstActivity == context::class.java) {
            cb.isChecked = true
        }
        setOnClickListener {
            cb.toggle()
            DataHelper.firstActivity = if (cb.isChecked) context::class.java else null
        }
    }
}
