package com.munch.lib.recyclerview

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.extend.ViewCreator

/**
 * @param provider 用于创建item视图
 * @param adapterFun 用于处理数据
 * @param clickHelper 用于处理点击等事件
 *
 * Create by munch1182 on 2022/3/31 14:17.
 */
abstract class BaseRecyclerViewAdapter<D, VH : BaseViewHolder>(
    private val provider: VHProvider,
    private val adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
    private val clickHelper: AdapterClickHandler<VH> = AdapterListenerHelper(),
) : RecyclerView.Adapter<VH>(),
    VHProvider by provider,
    IAdapterFun<D> by adapterFun,
    AdapterClickListener<VH> by clickHelper {

    constructor(
        @LayoutRes res: Int = 0,
        adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
        clickHelper: AdapterClickHandler<VH> = AdapterListenerHelper()
    ) : this(SimpleVHProvider(res), adapterFun, clickHelper)

    constructor(
        viewCreator: ViewCreator,
        adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
        clickHelper: AdapterClickHandler<VH> = AdapterListenerHelper()
    ) : this(SimpleVHProvider(viewCreator), adapterFun, clickHelper)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        adapterFun.bindAdapter(this)
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun <VH> provideVH(parent: ViewGroup, viewType: Int) =
        vhCreator.get(viewType).onCreateVH(parent) as VH

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        provideVH<VH>(parent, viewType)

    override fun getItemViewType(position: Int): Int {
        val d = get(position)
        return if (d is ItemType) d.getItemType(position) else 0
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        handleClick(holder)
        onBind(holder, get(position)!!)
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        //拦截了RecyclerView.Adapter的bind事件传递
        onBind(holder, get(position)!!, payloads)
    }

    protected open fun onBind(holder: VH, bean: D, payloads: MutableList<Any>) {
        //重新传递RecyclerView.Adapter的bind事件
        super.onBindViewHolder(holder, holder.bindingAdapterPosition, payloads)
    }

    abstract fun onBind(holder: VH, bean: D)

    override fun getItemCount() = itemSize

    @Suppress("UNCHECKED_CAST")
    private fun handleClick(holder: VH) {
        holder.setOnItemClickListener(clickHelper.itemClickListener as? OnItemClickListener<BaseViewHolder>?)
        holder.setOnItemLongClickListener(clickHelper.itemLongClickListener as? OnItemClickListener<BaseViewHolder>?)
        if (clickHelper.clickIds.isNotEmpty()) {
            holder.setOnViewClickListener(
                clickHelper.viewClickListener as? OnItemClickListener<BaseViewHolder>?,
                *clickHelper.clickIds
            )
        }
        if (clickHelper.longClickIds.isNotEmpty()) {
            holder.setOnViewClickListener(
                clickHelper.viewLongClickListener as? OnItemClickListener<BaseViewHolder>?,
                *clickHelper.longClickIds
            )
        }
    }
}