package com.munch.lib.android.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * 处理RecyclerViewAdapter除了item布局和显示之外的其它逻辑
 */
abstract class BaseRecyclerViewAdapter<D, VH : RecyclerView.ViewHolder>(
    private val dataHelper: AdapterFunImp<D>
) : RecyclerView.Adapter<VH>(),
    AdapterDataFun<D> by dataHelper {

    protected abstract val vhProvider: VHProvider<VH>

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        dataHelper.bindAdapter(this)
    }

    @Suppress("unchecked_cast")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        vhProvider.provideVH(parent, viewType)

    override fun onBindViewHolder(holder: VH, position: Int) {
    }

    override fun getItemCount() = dataHelper.itemSize
}

/**
 * 基类VH
 */
open class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view)

/**
 * 将ViewHolder的创建交由外部去实现
 */
interface VHProvider<VH : RecyclerView.ViewHolder> {

    fun provideVH(parent: ViewGroup, viewType: Int): VH
}

/**
 * 用于给其它需要adapter对象的类提供adapter
 */
interface AdapterProvider {
    fun bindAdapter(adapter: BaseRecyclerViewAdapter<*, *>)
}

/**
 * adapter的数据处理方法
 */
interface AdapterDataFun<D> {

    val itemSize: Int

    fun add(index: Int, data: D) // 在指定位置添加一个数据
    fun add(data: D) = add(itemSize, data) // 在数据末尾添加一个数据
    fun add(index: Int, data: Collection<D>) // 在指定位置添加一些数据
    fun add(data: Collection<D>) = add(itemSize, data) // 在数据末尾添加一些数据

    fun remove(index: Int) // 移除指定位置的数据
    fun remove(data: D) // 移除该数据
    fun remove(from: Int, size: Int) // 从from开始,包括from,移除共size个数据
    fun remove(data: Collection<D>) // 移除这些数据

    fun update(index: Int, data: D) // 更新指定位置的数据
    // fun update(data: D) // 更新与这个数据的HashCode值相等的数据为这个数据
    //fun update(from: Int, data: Collection<D>)

    fun get(index: Int): D // 获取指定位置的数据, 如果该位置没有数据, 则会抛出异常
    fun getOrNull(index: Int): D? { // 获取指定位置的数据, 如果该位置没有数据, 则会返回null
        if (index < 0 || index >= itemSize) return null
        return get(index)
    }

    fun find(data: D): Int? // 获取该数据所在的位置
}

/**
 * 给AdapterDataFun提供AdapterProvider
 * 使用AdapterDataFun by AdapterFunImp的方式可以实现AdapterDataFun并?隐藏AdapterProvider
 */
interface AdapterFunImp<D> : AdapterDataFun<D>, AdapterProvider
