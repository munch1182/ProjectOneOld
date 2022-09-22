package com.munch.lib.fast.view.dispatch

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.view.children
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.extend.*
import com.munch.lib.fast.view.DataHelper
import com.munch.lib.fast.view.dialog.DialogHelper

interface IConfigDialog : ActivityDispatch {

    override fun onCreateActivity(activity: AppCompatActivity) {
        super.onCreateActivity(activity)
        val last = activity.contentView.parent.to<ViewGroup>().children
            .lastOrNull()?.to<ViewGroup>() // actionBarContainer
            ?.children?.firstOrNull() // toolbar
        last?.setDoubleClickListener { onCreateDialog(activity).show() }
    }

    fun onCreateDialog(activity: AppCompatActivity): IDialog =
        DialogHelper.bottom()
            .content(newCheck(activity))
            .title(activity::class.java.simpleName.replace("Activity", ""))

    fun newCheck(activity: AppCompatActivity): View {
        return FrameLayout(activity).apply {
            padding(horizontal = 16.dp2Px2Int())
            addView(AppCompatCheckBox(activity).apply {
                textSize = 16f
                gravity = Gravity.CENTER_VERTICAL
                clickEffect()

                isChecked =
                    DataHelper.firstPage?.canonicalName == activity::class.java.canonicalName

                text = "默认跳转到此页"

                setOnCheckedChangeListener { _, isChecked ->
                    DataHelper.setFirstPage(if (isChecked) activity::class else null)
                }
            })
        }
    }
}

class SupportConfigDialog : IConfigDialog {
    override val dispatchers: MutableList<ActivityDispatch> = mutableListOf()
}