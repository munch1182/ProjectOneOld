package com.munch.pre.lib.base.rv

import android.view.View
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.munch.pre.lib.base.listener.ViewIntTagClickListener
import com.munch.pre.lib.base.listener.ViewIntTagLongClickListener

/**
 * Create by munch1182 on 2021/3/31 14:41.
 */
abstract class BaseAdapter<D, V : BaseViewHolder>(dataInt: MutableList<D>? = null) :
    RecyclerView.Adapter<V>(), FastFun<D, V> {

    protected open val dataList: MutableList<D>
            by lazy { if (dataInt.isNullOrEmpty()) mutableListOf() else ArrayList(dataInt) }
    protected var itemClickListener: ItemClickListener<D, V>? = null
    protected var itemLongClickListener: ItemClickListener<D, V>? = null
    protected open val viewClickListener by lazy {
        object : ViewIntTagClickListener {
            override fun onClick(v: View, index: Int) {
                itemClickListener?.onItemClick(this@BaseAdapter, getData()[index], v, index)
            }
        }
    }
    protected open val viewLongClickListener by lazy {
        object : ViewIntTagLongClickListener {
            override fun onLongClick(v: View, index: Int): Boolean {
                itemLongClickListener?.onItemClick(this@BaseAdapter, getData()[index], v, index)
                    ?: return false
                return true
            }
        }
    }

    override fun onBindViewHolder(holder: V, position: Int) {
        onBindViewHolder(holder, getData()[position], position)
        holder.itemView.tag = position
        if (itemClickListener != null) {
            holder.itemView.setOnClickListener(viewClickListener)
        }
        if (itemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(viewLongClickListener)
        }
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
}

/**
 * 更适合经常变更数据源的，比如重数据库或者后台返回的数据
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

}