package com.munch.pre.lib.base.rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.munch.pre.lib.base.listener.ViewIntTagClickListener
import com.munch.pre.lib.base.listener.ViewIntTagLongClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Create by munch1182 on 2021/3/31 14:41.
 */
abstract class BaseAdapter<D, V : BaseViewHolder> constructor(
    @LayoutRes protected open var itemRes: Int = 0,
    protected open var itemView: View? = null,
    dataInit: MutableList<D>? = null,
    protected open var diffUtil: DiffUtil.ItemCallback<D>? = null
) : RecyclerView.Adapter<V>() {

    @NonNull
    protected val dataList: MutableList<D> = mutableListOf()
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

    init {
        if (!dataInit.isNullOrEmpty()) {
            dataList.addAll(dataInit)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V {
        val itemView = onCreateItemView(parent)
        return BaseViewHolder(itemView) as V
    }

    protected open fun onCreateItemView(parent: ViewGroup): View = when {
        itemRes != 0 -> LayoutInflater.from(parent.context).inflate(itemRes, parent, false)
        itemView != null -> {
            if (itemView!!.parent != null) {
                (itemView!!.parent as ViewGroup).removeView(itemView!!)
            }
            itemView!!
        }
        else -> throw IllegalStateException("cannot create view holder without item view")
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

    @NonNull
    open fun getData() = dataList

    open fun get(index: Int): D? = getData()[index]

    open fun add(bean: D, index: Int = -1) {
        val pos: Int
        if (index == -1) {
            pos = getData().size
            getData().add(bean)
        } else {
            pos = index
            getData().add(index, bean)
        }
        notifyItemInserted(pos)
    }

    open fun add(beanList: MutableList<D>?, index: Int = -1) {
        if (!beanList.isNullOrEmpty()) {
            val start: Int
            if (index == -1) {
                start = getData().size
                getData().addAll(beanList)
            } else {
                start = index
                getData().addAll(index, beanList)
            }
            notifyItemRangeInserted(start, beanList.size)
        }
    }

    open fun set(beanList: MutableList<D>?) {
        if (!beanList.isNullOrEmpty()) {
            if (diffUtil != null) {
                runBlocking {
                    withContext(Dispatchers.Default) {
                        DiffUtil.calculateDiff(DiffCallBack(getData(), beanList, diffUtil!!))
                    }
                }.dispatchUpdatesTo(this)
            }
            getData().clear()
            getData().addAll(beanList)
            if (diffUtil == null) {
                notifyDataSetChanged()
            }
        } else {
            getData().clear()
            notifyDataSetChanged()
        }
    }

    open fun set(index: Int, bean: D) {
        getData()[index] = bean
        notifyItemChanged(index)
    }

    open fun remove(index: Int): D {
        val element = getData().removeAt(index)
        notifyItemRemoved(index)
        return element
    }

}

private class DiffCallBack<D>(
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

interface ItemClickListener<D, V : BaseViewHolder> {

    fun onItemClick(adapter: BaseAdapter<D, V>, bean: D, view: View, pos: Int)
}

open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)