package com.munch.lib.fast.view

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.munch.lib.android.dialog.DialogInterface
import com.munch.lib.android.extend.contentView
import com.munch.lib.android.extend.setDoubleClickListener
import com.munch.lib.android.extend.to

/**
 * Created by munch1182 on 2022/4/17 2:15.
 */
interface ISupportConfigDialog : ActivityDispatch {

    override fun onCreateActivity(activity: AppCompatActivity) {
        super.onCreateActivity(activity)

        val last = activity.contentView.parent.to<ViewGroup>().children
            .lastOrNull()?.to<ViewGroup>() // actionBarContainer
            ?.children?.firstOrNull() // toolbar
        //双击toolbar弹出ConfigDialog
        last?.setDoubleClickListener { onCreateDialog(activity).show() }
    }

    /**
     * 在此方法中创建Activity，此方法只会被调用一次
     */
    fun onCreateDialog(activity: AppCompatActivity): DialogInterface
}