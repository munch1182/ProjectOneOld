package com.munch.pre.lib.base.rv

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

interface ItemClickListener<D, V : BaseViewHolder> {

    fun onItemClick(adapter: BaseAdapter<D, V>, bean: D, view: View, pos: Int)
}

open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

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