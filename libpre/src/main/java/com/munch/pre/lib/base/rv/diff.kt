package com.munch.pre.lib.base.rv

import androidx.recyclerview.widget.DiffUtil

class DiffCallBack<D>(
    private val old: MutableList<D>,
    private val new: MutableList<D>,
    private val itemCallback: DiffUtil.ItemCallback<D>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return old.size
    }

    override fun getNewListSize(): Int {
        return new.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val o = old[oldItemPosition] ?: return false
        val n = new[newItemPosition] ?: return false
        return itemCallback.areItemsTheSame(o, n)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val o = old[oldItemPosition] ?: return false
        val n = new[newItemPosition] ?: return false
        return itemCallback.areContentsTheSame(o, n)
    }
}

abstract class DiffItemCallback<D> : DiffUtil.ItemCallback<D>() {
    override fun areItemsTheSame(oldItem: D, newItem: D): Boolean {
        return oldItem == newItem
    }
}