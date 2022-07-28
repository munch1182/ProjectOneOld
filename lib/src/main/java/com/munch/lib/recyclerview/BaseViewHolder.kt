package com.munch.lib.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.R

/**
 * Create by munch1182 on 2022/3/31 14:18.
 */
open class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view),
    AdapterClickListener<BaseViewHolder> {

    constructor(parent: ViewGroup, @LayoutRes resId: Int) : this(
        LayoutInflater.from(parent.context).inflate(resId, parent, false)
    )

    override fun setOnItemClickListener(listener: OnItemClickListener<BaseViewHolder>?) {
        itemView.setTag(R.id.id_tag, this)
        itemView.setOnClickListener(listener)
    }

    override fun setOnItemLongClickListener(listener: OnItemClickListener<BaseViewHolder>?) {
        itemView.setTag(R.id.id_tag, this)
        itemView.setOnLongClickListener(listener)
    }

    override fun setOnViewClickListener(
        listener: OnItemClickListener<BaseViewHolder>?,
        vararg ids: Int
    ) {
        ids.forEach { id ->
            itemView.findViewById<View>(id)?.let {
                itemView.setTag(R.id.id_tag, this)
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
                itemView.setTag(R.id.id_tag, this)
                it.setOnLongClickListener(listener)
            }
        }
    }
}