package com.munch.lib.fast.base

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.munch.lib.base.*
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

fun <ACT : BaseBigTextTitleActivity> ACT.newMenuBottomDialog(
    initView: ((LinearLayout) -> Unit)? = null,
    title: String = this::class.java.simpleName
): BottomSheetDialog {
    return commonBottomDialog(this).apply {
        setContentView(R.layout.layout_bottom_sheel_dialog)
        findViewById<LinearLayout>(R.id.dialog_container)?.apply {
            initView?.invoke(this)
            //添加通用设置
            addView(newCheck(this@newMenuBottomDialog))
        }
        findViewById<TextView>(R.id.dialog_title)?.text = title
    }
}

private fun commonBottomDialog(
    context: Context, @StyleRes theme: Int = R.style.AppTheme_BottomDialog_Trans
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

fun Context.newMenuDialog(
    title: String,
    names: Array<CharSequence>,
    clickListener: OnViewIndexClickListener
): AlertDialog.Builder {
    return newMenuDialog({
        names.mapIndexed { index, name ->
            newItemTextView(name).apply {
                tag = index
                setOnClickListener(clickListener)
            }
        }.forEach { tv -> it.addView(tv) }
    }, title)
}

fun Context.newItemTextView(str: CharSequence? = null): TextView {
    val dp16 = resources.getDimension(R.dimen.paddingDef).toInt()
    return TextView(this,null,0,R.style.AppTheme_Text_Subtitle).apply {
        text = str
        background = getSelectableItemBackground()
        setPadding(dp16, dp16 / 2, dp16, dp16 / 2)
    }
}

fun Context.newMenuDialog(
    initView: ((LinearLayout) -> Unit)? = null,
    title: String
): AlertDialog.Builder {
    val view = View.inflate(this, R.layout.layout_center_dialog, null)
    view.findViewById<TextView>(R.id.dialog_title)?.text = title
    initView?.invoke(view.findViewById(R.id.dialog_container))
    return AlertDialog.Builder(this, R.style.AppTheme_CenterDialog_Trans)
        .setView(view)

}
