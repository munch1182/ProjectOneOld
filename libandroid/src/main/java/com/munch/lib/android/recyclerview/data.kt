package com.munch.lib.android.recyclerview

import android.annotation.SuppressLint

/**
 * 默认实现, 不考虑线程
 */
class SimpleAdapterFun<D> : AdapterFunHelper<D> {

    private val list = mutableListOf<D>()
    private lateinit var adapter: BaseRecyclerViewAdapter<*, *>

    override fun bindAdapter(adapter: BaseRecyclerViewAdapter<*, *>) {
        this.adapter = adapter
    }

    override fun set(data: Collection<D>?) {
        val size = getItemCount()
        list.clear()
        if (data == null) {
            adapter.notifyItemRangeRemoved(0, size)
        } else {
            add(data)
        }
    }

    override fun getItemCount() = list.size

    override fun add(index: Int, data: D) {
        list.add(index, data)
        adapter.notifyItemInserted(index)
    }

    override fun add(index: Int, data: Collection<D>) {
        list.addAll(index, data)
        adapter.notifyItemRangeInserted(index, data.size)
    }

    override fun remove(index: Int) {
        list.removeAt(index)
        adapter.notifyItemRemoved(index)
    }

    override fun remove(from: Int, size: Int) {
        list.removeAll(list.subList(from, from + size))
        adapter.notifyItemRangeRemoved(from, size)
    }

    // 是否能更新, list对象没有更改
    @SuppressLint("NotifyDataSetChanged")
    override fun remove(data: Collection<D>) {
        list.removeAll(data)
        adapter.notifyDataSetChanged()
    }

    override fun update(index: Int, data: D) {
        list[index] = data
        adapter.notifyItemChanged(index)
    }

    override fun get(index: Int) = list[index]

    override fun find(data: D): Int? = list.indexOf(data).takeIf { it != -1 }
}