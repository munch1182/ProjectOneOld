package com.munch.project.launcher.base.recyclerview

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.munch.lib.helper.PhoneHelper

class StatusBarAdapter(context: Context) : BaseHolderAdapter<String>(
    View(context).apply {
        layoutParams = androidx.recyclerview.widget.RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            PhoneHelper.getStatusBarHeight()
        )
    }, arrayListOf("")
) {
    override fun onBind(holder: BaseViewHolder, data: String, position: Int) {
    }
}