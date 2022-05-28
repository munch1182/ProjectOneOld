package com.munch.lib.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2022/3/31 14:18.
 */
open class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view),
    AdapterClickListener<BaseViewHolder> {

    override fun setOnItemClickListener(listener: OnItemClickListener<BaseViewHolder>?) {
        itemView.tag = this
        itemView.setOnClickListener(listener)
    }

    override fun setOnItemLongClickListener(listener: OnItemClickListener<BaseViewHolder>?) {
        itemView.tag = this
        itemView.setOnLongClickListener(listener)
    }

    override fun setOnViewClickListener(
        listener: OnItemClickListener<BaseViewHolder>?,
        vararg ids: Int
    ) {
        ids.forEach { id ->
            itemView.findViewById<View>(id)?.let {
                it.tag = this
                it.setOnClickListener(listener)
            }
        }
    }

    override fun setOnViewLongClickListener(
        listener: OnItemClickListener<BaseViewHolder>?,
        vararg ids: Int
    ) {
        ids.forEach { id ->
            itemView.findViewById<View>(id)?.let {
                it.tag = this
                it.setOnLongClickListener(listener)
            }
        }
    }
}