package com.munch.pre.lib.base.rv

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.munch.pre.lib.log.log

interface ItemClickListener<D, V : BaseViewHolder> {

    fun onItemClick(adapter: BaseAdapter<D, V>, bean: D, view: View, pos: Int)
}

open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun <D, V : BaseViewHolder> setOnItemClickListener(
        itemClick: ItemClickListener<D, V>,
        adapter: BaseAdapter<D, V>
    ) {
        itemView.setOnClickListener {
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }
            itemClick.onItemClick(adapter, adapter.getData()[position], it, position)
        }
    }

    fun <D, V : BaseViewHolder> setOnItemLongClickListener(
        longClick: ItemClickListener<D, V>,
        adapter: BaseAdapter<D, V>
    ) {
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

class ItemDiffCallBack<D>(vararg parameter: (D) -> Any) : DiffUtil.ItemCallback<D>() {

    private val diff = parameter

    /**
     * 决定两个数据是否是同一个
     */
    override fun areItemsTheSame(oldItem: D, newItem: D): Boolean {
        diff.forEach {
            if (it.invoke(oldItem) != it.invoke(newItem)) {
                return false
            }
        }
        return true
    }

    /**
     * 当两个数据是同一个是，是否要进行更新
     */
    override fun areContentsTheSame(oldItem: D, newItem: D): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }
}