package com.munch.lib.android.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Create by munch1182 on 2022/3/31 14:18.
 */
open class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view), AdapterClickListener {

    override fun setOnItemClickListener(listener: OnItemClickListener?) {
        itemView.tag = this
        itemView.setOnClickListener(listener)
    }

    override fun setOnItemLongClickListener(listener: OnItemClickListener?) {
        itemView.tag = this
        itemView.setOnLongClickListener(listener)
    }

    override fun setOnViewClickListener(listener: OnItemClickListener?, vararg ids: Int) {
        ids.forEach { id ->
            itemView.findViewById<View>(id)?.let {
                it.tag = this
                it.setOnClickListener(listener)
            }
        }
    }

    override fun setOnViewLongClickListener(listener: OnItemClickListener?, vararg ids: Int) {
        ids.forEach { id ->
            itemView.findViewById<View>(id)?.let {
                it.tag = this
                it.setOnLongClickListener(listener)
            }
        }
    }
}

open class BaseBindViewHolder<B : ViewBinding>(open val bind: B) : BaseViewHolder(bind.root)