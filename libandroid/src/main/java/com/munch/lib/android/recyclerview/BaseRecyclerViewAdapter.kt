package com.munch.lib.android.recyclerview

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.munch.lib.android.R
import com.munch.lib.android.extend.toOrNull

/**
 * 处理RecyclerViewAdapter除了item布局和显示之外的其它逻辑
 */
abstract class BaseRecyclerViewAdapter<D, VH : BaseViewHolder>(
    protected open val vhProvider: VHProvider<VH>?,
    protected open val dataHelper: AdapterFunHelper<D> = SimpleAdapterFun(),
    protected open val eventHelper: AdapterEventHelper<VH> = ClickHelper()
) : RecyclerView.Adapter<VH>(),
    AdapterDataFun<D> by dataHelper,
    AdapterEvent<VH> by eventHelper {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        dataHelper.bindAdapter(this)
    }

    @Suppress("unchecked_cast")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        vhProvider?.provideVH(parent, viewType)?.apply { eventHelper.onCreate(this) }
            ?: throw NullPointerException("must provider item view")

    @Suppress("unchecked_cast")
    override fun onBindViewHolder(holder: VH, position: Int) {
        eventHelper.onBind(holder)
        onBind(holder, get(position))
    }

    override fun getItemCount() = dataHelper.getItemCount()

    abstract fun onBind(holder: VH, bean: D)
}

//<editor-fold desc="VH">
/**
 * 基类VH
 */
open class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val pos: Int
        get() = bindingAdapterPosition

    open fun <V : View> get(viewId: Int): V? = itemView.findViewById(viewId)

    @Suppress("unchecked_cast")
    open fun <VH : BaseViewHolder> get(): VH? = itemView.getTag(R.id.id_vh).toOrNull()
}

/**
 * bindVH
 */
open class BaseBindViewHolder<VB : ViewBinding>(open val bind: VB) : BaseViewHolder(bind.root)
//</editor-fold>

//<editor-fold desc="view">
/**
 * 将ViewHolder的创建交由外部去实现
 */
interface VHProvider<VH : BaseViewHolder> {

    fun provideVH(parent: ViewGroup, viewType: Int): VH
}
//</editor-fold>

//<editor-fold desc="data">
/**
 * 用于给其它需要adapter对象的类提供adapter
 */
interface AdapterProvider {
    fun bindAdapter(adapter: BaseRecyclerViewAdapter<*, *>)
}

/**
 * adapter的数据处理方法
 *
 * 考虑到大部分都是数据不为null的情形, 所以数据不能为null, 可以减少很多判断
 * 如果需要为null, 可以单独设置属性来表示
 */
interface AdapterDataFun<D> {

    fun getItemCount(): Int

    fun set(data: Collection<D>?)

    //<editor-fold desc="add">
    fun add(index: Int, data: D) // 在指定位置添加一个数据
    fun add(data: D) = add(getItemCount(), data) // 在数据末尾添加一个数据
    fun add(index: Int, data: Collection<D>) // 在指定位置添加一些数据
    fun add(data: Collection<D>) = add(getItemCount(), data) // 在数据末尾添加一些数据
    //</editor-fold>

    //<editor-fold desc="remove">
    fun remove(index: Int) // 移除指定位置的数据
    fun remove(data: D) = find(data)?.let { remove(it) }  // 移除该数据
    fun remove(from: Int, size: Int) // 从from开始,包括from,移除共size个数据
    fun remove(data: Collection<D>) // 移除这些数据
    //</editor-fold>

    //<editor-fold desc="update">
    fun update(index: Int, data: D) // 更新指定位置的数据
    fun update(data: D) = find(data)?.let { update(it, data) }// 更新与这个数据的HashCode值相等的数据为这个数据
    //fun update(from: Int, data: Collection<D>)
    //</editor-fold>

    //<editor-fold desc="get">
    fun get(index: Int): D // 获取指定位置的数据, 如果该位置没有数据, 则会抛出异常
    fun getOrNull(index: Int): D? { // 获取指定位置的数据, 如果该位置没有数据, 则会返回null
        if (index < 0 || index >= getItemCount()) return null
        return get(index)
    }

    fun find(data: D): Int? // 获取该数据所在的位置

    fun contain(data: D): Boolean = find(data) != null
    val isEmpty: Boolean
        get() = getItemCount() == 0
    //</editor-fold>
}

/**
 * 给AdapterDataFun提供AdapterProvider
 * 使用AdapterDataFun by AdapterFunImp的方式可以实现AdapterDataFun并?隐藏AdapterProvider
 */
interface AdapterFunHelper<D> : AdapterDataFun<D>, AdapterProvider
//</editor-fold>

//<editor-fold desc="click">
fun interface OnItemClickListener<VH : BaseViewHolder> {
    fun onItemClick(holder: VH)
}

fun interface OnItemLongClickListener<VH : BaseViewHolder> {
    fun onItemLongClick(holder: VH)
}

fun interface OnItemViewClickListener<VH : BaseViewHolder> {
    fun onItemClick(view: View, holder: VH)
}

fun interface OnItemViewLongClickListener<VH : BaseViewHolder> {
    fun onItemLongClick(view: View, holder: VH)
}

/**
 * 处理adapter的点击事件回调
 */
interface AdapterEvent<VH : BaseViewHolder> {
    fun setOnItemClick(l: OnItemClickListener<VH>?): AdapterEvent<VH>
    fun setOnItemLongClick(l: OnItemLongClickListener<VH>?): AdapterEvent<VH>
    fun setOnItemViewClick(viewId: Int, l: OnItemViewClickListener<VH>?): AdapterEvent<VH>
    fun setOnItemViewLongClick(viewId: Int, l: OnItemViewLongClickListener<VH>?): AdapterEvent<VH>
}

/**
 * 处理并分发adapter的点击事件
 */
interface AdapterEventHelper<VH : BaseViewHolder> : AdapterEvent<VH> {
    var itemClick: OnItemClickListener<VH>?
    var itemLongClick: OnItemLongClickListener<VH>?
    var viewClick: SparseArray<OnItemViewClickListener<VH>?>?
    var viewLongClick: SparseArray<OnItemViewLongClickListener<VH>?>?

    override fun setOnItemClick(l: OnItemClickListener<VH>?): AdapterEvent<VH> {
        this.itemClick = l
        return this
    }

    override fun setOnItemLongClick(l: OnItemLongClickListener<VH>?): AdapterEvent<VH> {
        this.itemLongClick = l
        return this
    }

    override fun setOnItemViewClick(
        viewId: Int,
        l: OnItemViewClickListener<VH>?
    ): AdapterEvent<VH> {
        if (viewClick == null) {
            viewClick = SparseArray()
        }
        viewClick!![viewId] = l
        return this
    }

    override fun setOnItemViewLongClick(
        viewId: Int,
        l: OnItemViewLongClickListener<VH>?
    ): AdapterEvent<VH> {
        if (viewLongClick == null) {
            viewLongClick = SparseArray()
        }
        viewLongClick!![viewId] = l
        return this
    }

    fun onCreate(vh: VH)
    fun onBind(vh: VH)
}
//</editor-fold>