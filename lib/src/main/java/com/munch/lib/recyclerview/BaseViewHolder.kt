package com.munch.lib.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2021/8/5 16:44.
 */
open class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view), AdapterListener {

    override fun setOnItemClickListener(listener: OnItemClickListener?) {
        itemView.tag = this
        itemView.setOnClickListener(listener)
    }

    override fun setOnItemLongClickListener(listener: OnItemClickListener?) {
        itemView.tag = this
        itemView.setOnLongClickListener(listener)
    }

    override fun setOnViewClickListener(listener: OnItemClickListener?, ids: MutableList<Int>) {
        ids.forEach { id ->
            itemView.findViewById<View>(id)?.let {
                it.tag = this
                it.setOnClickListener(listener)
            }
        }
    }

    override fun setOnViewLongClickListener(listener: OnItemClickListener?, ids: MutableList<Int>) {
        ids.forEach { id ->
            itemView.findViewById<View>(id)?.let {
                it.tag = this
                it.setOnLongClickListener(listener)
            }
        }
    }
}