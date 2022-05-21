package com.munch.lib.recyclerview

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.extend.ViewCreator

/**
 * @param viewImp 用于创建item视图
 * @param adapterFun 用于处理数据
 * @param clickHelper 用于处理点击等事件
 *
 * Create by munch1182 on 2022/3/31 14:17.
 */
abstract class BaseRecyclerViewAdapter<D, VH : BaseViewHolder>(
    private val viewImp: AdapterViewImp<VH>,
    private val adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
    private val clickHelper: AdapterClickHandler<VH> = AdapterListenerHelper(),
) : RecyclerView.Adapter<VH>(),
    IAdapterFun<D> by adapterFun,
    AdapterClickListener<VH> by clickHelper {

    constructor(
        @LayoutRes res: Int = 0,
        adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
        clickHelper: AdapterClickHandler<VH> = AdapterListenerHelper()
    ) : this(viewImp = SingleVHCreator(res), adapterFun, clickHelper)

    constructor(
        viewCreator: ViewCreator,
        adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
        clickHelper: AdapterClickHandler<VH> = AdapterListenerHelper()
    ) : this(SingleVHCreator(viewCreator = viewCreator), adapterFun, clickHelper)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        adapterFun.bindAdapter(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        viewImp.createVH(parent, viewType)

    override fun getItemViewType(position: Int) = viewImp.getItemViewTypeByPos(position)

    override fun onBindViewHolder(holder: VH, position: Int) {
        handleClick(holder)
        onBind(holder, holder.bindingAdapterPosition, get(position)!!)
    }

    abstract fun onBind(holder: VH, position: Int, bean: D)

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