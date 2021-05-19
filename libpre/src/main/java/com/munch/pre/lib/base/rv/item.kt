package com.munch.pre.lib.base.rv

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

interface ItemClickListener<D, V : BaseViewHolder> {

    fun onItemClick(adapter: BaseAdapter<D, V>, bean: D, view: View, pos: Int)
}

open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun <D, V : BaseViewHolder> setOnItemClickListener(
        itemClick: ItemClickListener<D, V>?,
        adapter: BaseAdapter<D, V>
    ) {
        if (itemClick == null) {
            itemView.setOnClickListener(null)
        } else {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return@setOnClickListener
                }
                itemClick.onItemClick(adapter, adapter.getData()[position], it, position)
            }
        }
    }

    fun <D, V : BaseViewHolder> setOnItemLongClickListener(
        longClick: ItemClickListener<D, V>?,
        adapter: BaseAdapter<D, V>
    ) {
        if (longClick == null) {
            itemView.setOnLongClickListener(null)
        } else {
            itemView.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return@setOnLongClickListener false
                }
                longClick.onItemClick(adapter, adapter.getData()[position], it, position)
                return@setOnLongClickListener true
            }
        }
    }

}

class ItemDiffCallBack<D>(vararg parameter: (D) -> Any) : DiffUtil.ItemCallback<D>() {

    private val diff = parameter

    override fun areItemsTheSame(oldItem: D, newItem: D): Boolean {
        diff.forEach {
            if (it.invoke(oldItem) != it.invoke(newItem)) {
                return false
            }
        }
        return true
    }

    override fun areContentsTheSame(oldItem: D, newItem: D): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }
}