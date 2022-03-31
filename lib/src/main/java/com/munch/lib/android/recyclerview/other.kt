package com.munch.lib.android.recyclerview

import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView

/**
 * Create by munch1182 on 2022/3/31 22:31.
 */
class EmptyAdapter(private var emptyNotice: CharSequence = "") :
    BaseRecyclerViewAdapter<Int, BaseViewHolder>() {

    override fun createVH(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            text = emptyNotice
        })
    }

    override fun onBind(holder: BaseViewHolder, position: Int, bean: Int) {
    }

    fun hide() {
        remove(index = 0)
    }

    fun show() {
        add(0)
    }

}