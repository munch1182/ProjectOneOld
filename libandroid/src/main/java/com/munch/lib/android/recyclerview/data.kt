package com.munch.lib.android.recyclerview

import android.annotation.SuppressLint
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.android.extend.to

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
        adapter.notifyItemRangeRemoved(0, size)
        if (data != null) {
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

fun interface DifferProvider<D> {
    fun provideDiffer(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>): AsyncListDiffer<D>?
}

/**
 * 使用了AsyncListDiffer的实现
 */
class DifferAdapterFun<D>(
    private var differProvider: DifferProvider<D>? = null
) : AdapterFunHelper<D> {

    constructor(callback: DiffUtil.ItemCallback<D>) : this({ AsyncListDiffer<D>(it, callback) })


    private var differ: AsyncListDiffer<D>? = null
    private lateinit var adapter: BaseRecyclerViewAdapter<*, *>
    private val curr: List<D>?
        get() = differ?.currentList
    private val newData: MutableList<D>
        get() = curr?.toMutableList() ?: mutableListOf()

    override fun bindAdapter(adapter: BaseRecyclerViewAdapter<*, *>) {
        this.adapter = adapter
        differ = differProvider?.provideDiffer(adapter.to())
    }

    override fun set(data: Collection<D>?) {
        differ?.submitList(data?.toList())
    }

    override fun getItemCount() = curr?.size ?: 0

    override fun add(index: Int, data: D) {
        set(newData.apply { add(index, data) })
    }

    override fun add(index: Int, data: Collection<D>) {
        set(newData.apply { addAll(index, data) })
    }

    override fun remove(index: Int) {
        set(newData.apply { removeAt(index) })
    }

    override fun remove(from: Int, size: Int) {
        set(newData.apply { removeAll(newData.subList(from, from + size)) })
    }

    override fun remove(data: Collection<D>) {
        set(newData.apply { removeAll(data) })
    }

    override fun update(index: Int, data: D) {
        set(newData.apply { set(index, data) })
    }

    override fun get(index: Int): D = curr?.get(index)!!

    override fun find(data: D): Int? = curr?.indexOf(data)


}