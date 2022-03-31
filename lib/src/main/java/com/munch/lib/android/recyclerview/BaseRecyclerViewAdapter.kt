package com.munch.lib.android.recyclerview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2022/3/31 14:17.
 */
abstract class BaseRecyclerViewAdapter<D, VH : BaseViewHolder>(
    private val viewImp: AdapterViewImp<VH>,
    private val clickHelper: AdapterClickHandler = AdapterListenerHelper(),
) : RecyclerView.Adapter<VH>(),
    IsAdapter,
    IAdapterFun<D>,
    AdapterViewImp<VH> by viewImp,
    AdapterClickListener by clickHelper {

    protected open val adapterFunImp: IAdapterFun<D> by lazy { AdapterFunImp.Default(this) }

    override val adapter: BaseRecyclerViewAdapter<*, *>
        get() = this

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return viewImp.createVH(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return viewImp.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        handleClick(holder)
        onBind(holder, holder.bindingAdapterPosition, get(position)!!)
    }

    abstract fun onBind(holder: VH, position: Int, bean: D)

    override fun getItemCount() = adapterFunImp.data.size

    private fun handleClick(holder: VH) {
        holder.setOnItemClickListener(clickHelper.itemClickListener)
        holder.setOnItemLongClickListener(clickHelper.itemLongClickListener)
        if (clickHelper.clickIds.isNotEmpty()) {
            holder.setOnViewClickListener(clickHelper.viewClickListener, *clickHelper.clickIds)
        }
        if (clickHelper.longClickIds.isNotEmpty()) {
            holder.setOnViewClickListener(
                clickHelper.viewLongClickListener, *clickHelper.longClickIds
            )
        }
    }

    //<editor-fold desc="AdapterFun">
    override val data: List<D>
        get() = adapterFunImp.data

    override fun set(newData: List<D>?) = adapterFunImp.set(newData)
    override fun add(element: D) = adapterFunImp.add(element)
    override fun add(index: Int, element: D) = adapterFunImp.add(index, element)
    override fun add(elements: Collection<D>) = adapterFunImp.add(elements)
    override fun add(index: Int, elements: Collection<D>) = adapterFunImp.add(index, elements)
    override fun remove(element: D) = adapterFunImp.remove(element)
    override fun remove(index: Int) = adapterFunImp.remove(index)
    override fun remove(startIndex: Int, size: Int) = adapterFunImp.remove(startIndex, size)
    override fun remove(element: Collection<D?>) = adapterFunImp.remove(element)
    override fun get(index: Int) = adapterFunImp.get(index)
    override fun getIndex(element: D) = adapterFunImp.getIndex(element)
    override fun contains(element: D) = adapterFunImp.contains(element)
    override fun update(index: Int, element: D, payload: Any?) =
        adapterFunImp.update(index, element, payload)

    override fun updateOrThrow(index: Int, element: D, payload: Any?) =
        adapterFunImp.updateOrThrow(index, element, payload)

    override fun update(startIndex: Int, elements: Collection<D>, payload: Any?) =
        adapterFunImp.update(startIndex, elements, payload)

    override fun updateOrThrow(startIndex: Int, elements: Collection<D>, payload: Any?) =
        adapterFunImp.updateOrThrow(startIndex, elements, payload)
    //</editor-fold>
}