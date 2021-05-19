package com.munch.pre.lib.base.rv

import android.view.View
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2021/3/31 14:41.
 */
abstract class BaseAdapter<D, V : BaseViewHolder>(dataInt: MutableList<D>? = null) :
    RecyclerView.Adapter<V>(), FastFun<D, V> {

    protected open val dataList: MutableList<D>
            by lazy { if (dataInt.isNullOrEmpty()) mutableListOf() else ArrayList(dataInt) }
    protected var itemClickListener: ItemClickListener<D, V>? = null
    protected var itemLongClickListener: ItemClickListener<D, V>? = null

    override fun onBindViewHolder(holder: V, position: Int) {
        onBindViewHolder(holder, getData()[position], position)
        handClick(holder, position)
    }

    protected open fun handClick(holder: V, position: Int) {
        holder.setOnItemClickListener(itemClickListener, this)
        holder.setOnItemLongClickListener(itemLongClickListener, this)
    }

    override fun getItemCount(): Int = getData().size

    protected abstract fun onBindViewHolder(holder: V, bean: D, pos: Int)

    open fun setOnItemClickListener(listener: ((adapter: BaseAdapter<D, V>, bean: D, view: View, pos: Int) -> Unit)? = null): BaseAdapter<D, V> {
        if (listener == null) {
            this.itemClickListener = null
        } else {
            this.itemClickListener = object : ItemClickListener<D, V> {
                override fun onItemClick(
                    adapter: BaseAdapter<D, V>,
                    bean: D,
                    view: View,
                    pos: Int
                ) {
                    listener.invoke(adapter, bean, view, pos)
                }
            }
        }
        return this
    }

    open fun setOnItemLongClickListener(listener: ((adapter: BaseAdapter<D, V>, bean: D, view: View, pos: Int) -> Unit)? = null): BaseAdapter<D, V> {
        if (listener == null) {
            this.itemLongClickListener = null
        } else {
            this.itemLongClickListener = object : ItemClickListener<D, V> {
                override fun onItemClick(
                    adapter: BaseAdapter<D, V>,
                    bean: D,
                    view: View,
                    pos: Int
                ) {
                    listener.invoke(adapter, bean, view, pos)
                }
            }
        }
        return this
    }

    override fun getData(): MutableList<D> = dataList

    override fun getAdapter(): BaseAdapter<D, V> = this

    @Suppress("UNCHECKED_CAST")
    open fun sort() {
        if (getData().isEmpty()) {
            return
        }
        val d = getData()[0]
        if (d is Comparable<*>) {
            (getData() as MutableList<Comparable<D>>).sort()
        } else {
            throw UnsupportedOperationException()
        }
        notifyDataSetChanged()
    }
}

/**
 * 更适合经常变更数据源的，比如从数据库或者后台返回的数据
 *
 * @see androidx.recyclerview.widget.ListAdapter
 */
abstract class BaseDifferAdapter<D, V : BaseViewHolder>(private val config: AsyncDifferConfig<D>) :
    BaseAdapter<D, V>(null) {

    private val differ by lazy { AsyncListDiffer(AdapterListUpdateCallback(this), config) }

    init {
        differ.addListListener { previousList, currentList ->
            onCurrentListChanged(previousList, currentList)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    protected open fun onCurrentListChanged(
        previousList: MutableList<D>,
        currentList: MutableList<D>
    ) {
    }

    private fun getNewList(): MutableList<D> = ArrayList(getData())

    fun submitList(newList: MutableList<D>, commitCallback: Runnable) {
        differ.submitList(newList, commitCallback)
    }

    fun submitList(newList: MutableList<D>) {
        differ.submitList(newList)
    }

    override fun getData(): MutableList<D> = differ.currentList

    override fun add(bean: D, index: Int) {
        val newList = getNewList()
        newList.add(index, bean)
        submitList(newList)
    }

    override fun add(beanList: MutableList<D>, index: Int) {
        if (!beanList.isNullOrEmpty()) {
            val newList = getNewList()
            newList.addAll(index, beanList)
            submitList(newList)
        }
    }

    override fun set(beanList: MutableList<D>?) {
        submitList(beanList ?: mutableListOf())
    }

    override fun set(index: Int, bean: D) {
        val newList = getNewList()
        newList[index] = bean
        submitList(newList)
    }

    @Suppress("UNCHECKED_CAST")
    override fun sort() {
        val data = getData()
        if (data.isEmpty()) {
            return
        }
        val d = data[0]
        if (d is Comparable<*>) {
            val newData = getNewList()
            (newData as MutableList<Comparable<D>>).sort()
            set(newData)
        } else {
            throw UnsupportedOperationException()
        }

    }
}