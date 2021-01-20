package com.munch.lib.extend.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
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

    protected var onClick: ((adapter: BaseAdapter<T, B>, view: View, data: T, pos: Int) -> Unit)? =
        null
    protected val onClickListener by lazy {
        View.OnClickListener {
            val pos = it.tag as? Int? ?: return@OnClickListener
            onClick?.invoke(this, it, getData(pos), pos)
        }
    }

    constructor(@LayoutRes resId: Int, list: MutableList<T>? = null) : this(resId, null, list)

    constructor(view: View, list: MutableList<T>? = null) : this(0, view, list)

    private val dataList: MutableList<T> = mutableListOf<T>().apply {
        if (list != null) {
            addAll(list)
        }
    }

    fun getData() = dataList

    fun getData(pos: Int) = getData()[pos]

    fun setData(data: MutableList<T>? = null) {
        this.dataList.clear()
        add(data)
    }

    fun setData(index: Int, data: T) {
        this.dataList[index] = data
        notifyItemChanged(index)
    }

    fun add(index: Int, data: T) {
        data ?: return
        this.dataList.add(index, data)
        notifyItemChanged(index)
    }

    fun add(data: MutableList<T>? = null) {
        data ?: return
        this.dataList.addAll(data)
        notifyDataSetChanged()
    }

    fun setOnItemClick(onClick: ((adapter: BaseAdapter<T, B>, view: View, data: T, pos: Int) -> Unit)? = null): BaseAdapter<T, B> {
        this.onClick = onClick
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): B {
        if (view != null) {
            return BaseViewHolder(view!!) as B
        } else if (resId != 0) {
            return BaseViewHolder(resId, parent) as B
        }
        throw Exception("未设置itemView")
    }

    override fun onBindViewHolder(holder: B, position: Int) {
        onBind(holder, dataList[position], position)
        onClick ?: return
        holder.itemView.tag = position
        holder.itemView.setOnClickListener(onClickListener)
    }

    abstract fun onBind(holder: B, data: T, position: Int)

    override fun getItemCount(): Int = dataList.size

}