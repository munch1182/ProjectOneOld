package com.munch.pre.lib.base.rv

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*

/**
 * Create by munch1182 on 2021/3/31 14:41.
 */
abstract class BaseAdapter<D, V : BaseViewHolder>(dataInt: MutableList<D>? = null) :
    RecyclerView.Adapter<V>(), FastFun<D, V> {

    protected open val dataList: MutableList<D>
            by lazy { if (dataInt.isNullOrEmpty()) mutableListOf() else ArrayList(dataInt) }
    protected open var itemClickListener: ItemClickListener<D, V>? = null
    protected open var itemLongClickListener: ItemClickListener<D, V>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V {
        return createVH(parent, viewType).apply { handClick(this) }
    }

    override fun onBindViewHolder(holder: V, position: Int) {
        onBindViewHolder(holder, getData()[position], position)
    }

    protected open fun handClick(holder: V) {
        holder.setOnItemClickListener(itemClickListener)
        holder.setOnItemLongClickListener(itemLongClickListener)
    }

    override fun getItemCount(): Int = getData().size

    protected abstract fun onBindViewHolder(holder: V, bean: D, pos: Int)
    protected abstract fun createVH(parent: ViewGroup, viewType: Int): V

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
    open suspend fun sort() {
        if (getData().size <= 1) {
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

    fun getNewList(): MutableList<D> = ArrayList(getData())

    fun submitList(newList: MutableList<D>, commitCallback: Runnable) {
        differ.submitList(newList, commitCallback)
    }

    fun submitList(newList: MutableList<D>) {
        differ.submitList(newList)
    }

    override fun getData(): MutableList<D> = differ.currentList

    override fun add(bean: D, index: Int) {
        val newList = getNewList()
        if (index != -1) {
            newList.add(index, bean)
        } else {
            newList.add(bean)
        }
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

    override fun remove(index: Int): D {
        val newList = getNewList()
        val bean = newList[index]
        newList.removeAt(index)
        set(newList)
        return bean
    }

    /**
     * 注意数据通过地址引用方式时，更改其属性然后排序，[getData]与[getNewList]是否会产生差异
     * 同时要注意[ConcatAdapter.Config.StableIdMode]设置下[androidx.recyclerview.widget.RecyclerView.Adapter.getItemId]的返回值
     * 这些都会影响是否正确生效
     */
    @Suppress("UNCHECKED_CAST")
    override suspend fun sort() {
        val data = getData()
        if (data.size <= 1) {
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