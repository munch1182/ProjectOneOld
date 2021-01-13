package com.munch.lib.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2021/1/13 9:33.
 */
open class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    constructor(
        @LayoutRes layoutResId: Int,
        parent: ViewGroup
    ) : this(LayoutInflater.from(parent.context).inflate(layoutResId, parent, false))
}
