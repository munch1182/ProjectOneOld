@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.project.launcher.base.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2021/1/13 9:33.
 */
class BaseSimpleAdapter<T>(
    @LayoutRes resId: Int = 0,
    list: MutableList<T>? = null,
    private val onBind: (holder: BaseViewHolder, data: T, position: Int) -> Unit
) : BaseHolderAdapter<T>(resId, list) {

    constructor(
        @LayoutRes resId: Int = 0,
        onBind: (holder: BaseViewHolder, data: T, position: Int) -> Unit
    ) : this(resId, null, onBind)

    override fun onBind(holder: BaseViewHolder, data: T, position: Int) {
        onBind.invoke(holder, data, position)
    }
}

abstract class BaseHolderAdapter<T>(
    @LayoutRes resId: Int = 0,
    list: MutableList<T>? = null
) : BaseAdapter<T, BaseViewHolder>(resId, list) {

    constructor(view: View? = null, list: MutableList<T>? = null) : this(0, list) {
        super.view = view
    }
}


abstract class BaseAdapter<T, B : BaseViewHolder> private constructor(
    @LayoutRes protected val resId: Int = 0,
    protected var view: View? = null,
    list: MutableList<T>? = null
) : RecyclerView.Adapter<B>() {

    private var diffUtil: DiffUtilCallback<T>? = null
    protected var onClick: ((adapter: BaseAdapter<T, B>, view: View, data: T, pos: Int) -> Unit)? =
        null
    protected var onLongClick: ((adapter: BaseAdapter<T, B>, view: View, data: T, pos: Int) -> Boolean)? =
        null
    protected val onClickListener by lazy {
        View.OnClickListener {
            val pos = it.tag as? Int? ?: return@OnClickListener
            onClick?.invoke(this, it, getData(pos), pos)
        }
    }
    protected val onLongClickListener by lazy {
        View.OnLongClickListener {
            val pos = it.tag as? Int? ?: return@OnLongClickListener false
            return@OnLongClickListener onLongClick?.invoke(this, it, getData(pos), pos) ?: false
        }
    }

    constructor(@LayoutRes resId: Int, list: MutableList<T>? = null) : this(resId, null, list)

    constructor(view: View, list: MutableList<T>? = null) : this(0, view, list)

    protected val dataList: MutableList<T> = mutableListOf<T>().apply {
        if (list != null) {
            addAll(list)
        }
    }

    fun setDiffUtil(diffUtil: DiffUtilCallback<T>) {
        this.diffUtil = diffUtil
    }

    fun getData() = dataList

    fun getData(pos: Int) = getData()[pos]

    fun setData(data: MutableList<T>? = null) {
        if (diffUtil != null) {
            //如果数据过多，建议异步
            DiffUtil.calculateDiff(diffUtil!!.updateData(this.dataList, data))
                .dispatchUpdatesTo(this)
        }
        this.dataList.clear()
        if (data != null) {
            this.dataList.addAll(data)
        }
        if (diffUtil != null) {
            notifyDataSetChanged()
        }
    }


    fun remove(from: Int, to: Int) {
        if (to <= from) {
            throw UnsupportedOperationException()
        }
        this.dataList.subList(from, to).clear()
        notifyItemRangeRemoved(from, to - from)
    }

    fun remove(index: Int) {
        this.dataList.removeAt(index)
        notifyItemRemoved(index)
    }

    fun remove(data: T) {
        remove(dataList.indexOf(data))
    }

    fun setData(index: Int, data: T) {
        this.dataList[index] = data
        notifyItemChanged(index, data)
    }

    fun add(index: Int, data: T) {
        data ?: return
        this.dataList.add(index, data)
        notifyItemInserted(index)
    }

    fun add(index: Int, data: MutableList<T>? = null) {
        data ?: return
        this.dataList.addAll(index, data)
        notifyItemRangeInserted(index, data.size)
    }

    fun add(data: MutableList<T>? = null) {
        if (data == null) {
            setData(data)
        } else {
            val index = this.dataList.size
            this.dataList.addAll(data)
            notifyItemChanged(index, data.size)
        }
    }

    fun setOnItemClick(onClick: ((adapter: BaseAdapter<T, B>, view: View, data: T, pos: Int) -> Unit)? = null): BaseAdapter<T, B> {
        this.onClick = onClick
        return this
    }

    fun setOnItemLongClick(onClick: ((adapter: BaseAdapter<T, B>, view: View, data: T, pos: Int) -> Boolean)? = null): BaseAdapter<T, B> {
        this.onLongClick = onClick
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): B {
        if (view != null) {
            return BaseViewHolder(view!!) as B
        } else if (resId != 0) {
            return BaseViewHolder(resId, parent) as B
        }
        throw UnsupportedOperationException("need itemView")
    }

    override fun onBindViewHolder(holder: B, position: Int) {
        onBind(holder, dataList[position], position)
        onClick ?: return
        holder.itemView.tag = position
        if (onClick != null) {
            holder.itemView.setOnClickListener(onClickListener)
        }
        if (onLongClick != null) {
            holder.itemView.setOnLongClickListener(onLongClickListener)
        }
    }

    abstract fun onBind(holder: B, data: T, position: Int)

    override fun getItemCount(): Int = dataList.size

}

abstract class DiffUtilCallback<T>(
    private var old: MutableList<T>? = null,
    private var new: MutableList<T>? = null
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return old?.size ?: 0
    }

    override fun getNewListSize(): Int {
        return new?.size ?: 0
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old?.get(oldItemPosition) ?: return false
        val newItem = new?.get(oldItemPosition) ?: return false
        return areItemsTheSame(oldItem, newItem)
    }

    abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean
    abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old?.get(oldItemPosition) ?: return false
        val newItem = new?.get(oldItemPosition) ?: return false
        return areContentsTheSame(oldItem, newItem)
    }

    fun updateData(old: MutableList<T>?, new: MutableList<T>?): DiffUtilCallback<T> {
        this.old = old
        this.new = new
        return this
    }
}