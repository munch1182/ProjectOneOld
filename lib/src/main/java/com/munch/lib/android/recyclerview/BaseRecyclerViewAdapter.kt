package com.munch.lib.android.recyclerview

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.android.helper.ViewCreator

/**
 * Create by munch1182 on 2022/3/31 14:17.
 */
abstract class BaseRecyclerViewAdapter<D, VH : BaseViewHolder>(
    private val viewImp: AdapterViewImp<VH>,
    private val adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
    private val clickHelper: AdapterClickHandler<VH> = AdapterListenerHelper(),
) : RecyclerView.Adapter<VH>(),
    AdapterViewImp<VH> by viewImp,
    IAdapterFun<D> by adapterFun,
    AdapterClickListener<VH> by clickHelper {

    constructor(@LayoutRes res: Int = 0) : this(viewImp = SingleVHCreator(res))

    constructor(viewCreator: ViewCreator) : this(SingleVHCreator(viewCreator = viewCreator))

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        adapterFun.bindAdapter(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createVH(parent, viewType)

    override fun getItemViewType(position: Int) = getItemViewTypeByPos(position)

    override fun onBindViewHolder(holder: VH, position: Int) {
        handleClick(holder)
        onBind(holder, holder.bindingAdapterPosition, get(position)!!)
    }

    abstract fun onBind(holder: VH, position: Int, bean: D)

    override fun getItemCount() = data.size

    private fun handleClick(holder: VH) {
        holder.setOnItemClickListener(clickHelper.itemClickListener as? OnItemClickListener<BaseViewHolder>?)
        holder.setOnItemLongClickListener(clickHelper.itemLongClickListener as? OnItemClickListener<BaseViewHolder>?)
        if (clickHelper.clickIds.isNotEmpty()) {
            holder.setOnViewClickListener(clickHelper.viewClickListener as? OnItemClickListener<BaseViewHolder>?, *clickHelper.clickIds)
        }
        if (clickHelper.longClickIds.isNotEmpty()) {
            holder.setOnViewClickListener(
                clickHelper.viewLongClickListener as? OnItemClickListener<BaseViewHolder>?, *clickHelper.longClickIds
            )
        }
    }
}