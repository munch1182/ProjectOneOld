package com.munch.pre.lib.base.rv

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface ItemClickListener<D, V : BaseViewHolder> {

    fun onItemClick(adapter: BaseAdapter<D, V>, bean: D, view: View, pos: Int)
}

open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)