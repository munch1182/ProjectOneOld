package com.munch.lib.fast.base

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleRes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.munch.lib.base.startActivity
import com.munch.lib.fast.R

/**
 * Create by munch1182 on 2021/8/14 9:06.
 */

fun <ACT : BaseBigTextTitleActivity> ACT.toSelectActivityIfHave() {
    DataHelper.selectedActivity?.let {
        try {
            startActivity(it)
        } catch (e: Exception) {
            DataHelper.selectedActivity = null
        }
    }
}

fun <ACT : BaseBigTextTitleActivity> ACT.newMenuDialog(
    initView: ((LinearLayout) -> Unit)? = null,
    title: String = this::class.java.simpleName
): BottomSheetDialog {
    return commonDialog(this).apply {
        setContentView(R.layout.layout_bottom_sheel_dialog)
        findViewById<LinearLayout>(R.id.dialog_container)?.apply {
            initView?.invoke(this)
            //添加通用设置
            addView(newCheck(this@newMenuDialog))
        }
        findViewById<TextView>(R.id.dialog_title)?.text = title
    }
}

private fun commonDialog(
    context: Context, @StyleRes theme: Int = R.style.AppTheme_BottomSheetDialog_Trans
) = BottomSheetDialog(context, theme)

private fun <ACT : BaseBigTextTitleActivity> newCheck(context: ACT): View {
    return View.inflate(context, R.layout.layout_item_checkbox, null).apply {
        val cb = findViewById<CheckBox>(R.id.item_cb_view)
        //设置不可点击，将点击事件交给Parent处理
        cb.isClickable = false
        //去掉toggle的水波纹效果
        cb.setBackgroundColor(Color.TRANSPARENT)
        if (DataHelper.selectedActivity == context::class.java) {
            cb.isChecked = true
        }
        setOnClickListener {
            cb.toggle()
            DataHelper.selectedActivity = if (cb.isChecked) context::class.java else null
        }
    }
}
