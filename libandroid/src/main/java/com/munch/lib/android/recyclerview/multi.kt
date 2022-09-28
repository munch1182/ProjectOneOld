package com.munch.lib.android.recyclerview

import android.util.ArrayMap
import android.util.SparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.android.extend.to

/**
 * 多布局
 *
 * 多布局的实现可以合并到[BaseRecyclerViewAdapter]中, 但考虑到其并不具有变化性, 因此还是分开来
 *
 * Create by munch1182 on 2022/9/27 14:52.
 */
abstract class BaseMultiRecyclerViewAdapter<D : Any, VH : RecyclerView.ViewHolder>(
    protected open val multiItem: SparseArray<IRecyclerItem<D, VH>> = SparseArray<IRecyclerItem<D, VH>>(),
    protected open val multiDataType: ArrayMap<Class<out Any>, Int> = ArrayMap<Class<out Any>, Int>(),
    dataHelper: AdapterFunHelper<D> = SimpleAdapterFun(),
    eventHelper: AdapterEventHelper<VH> = ClickHelper()
) : BaseRecyclerViewAdapter<D, VH>(null, dataHelper, eventHelper) {

    /**
     * 设置该类型的 类型[type], 数据类型[D]和ViewHolder[vhProvider] 以及视图和数据的bind方法
     */
    protected inline fun <reified ITEM : D> addItem(
        vhProvider: VHProvider<VH>,
        type: Int = multiItem.size(),
        noinline onBind: ((vh: VH, bean: ITEM) -> Unit)? = null
    ): BaseMultiRecyclerViewAdapter<D, VH> {
        val item = object : RecyclerItem<D, VH>(type, vhProvider) {
            override fun onBind(holder: VH, bean: D) {
                onBind?.invoke(holder, bean.to())
            }
        }
        multiItem.put(item.type, item.to())
        multiDataType[ITEM::class.java] = item.type
        return this
    }

    /**
     * 直接添加一个[IRecyclerItem]对象
     */
    protected inline fun <reified ITEM_D : D, ITEM_VH : VH> addItem(
        item: IRecyclerItem<ITEM_D, ITEM_VH>
    ): BaseMultiRecyclerViewAdapter<D, VH> {
        multiItem.put(item.type, item.to())
        multiDataType[ITEM_D::class.java] = item.type
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return multiItem.get(viewType).vhProvider.provideVH(parent)
            .apply { eventHelper.onCreate(this) }
    }

    override fun getItemViewType(position: Int): Int {
        val data = get(position)
        return multiDataType[data::class.java] ?: 0
    }

    override fun onBind(holder: VH, bean: D) {
        multiItem.get(holder.itemViewType)?.onBind(holder, bean)
    }
}

/**
 * 提供一个Item所需要的类型、视图和数据与试图的绑定
 *
 * 此处的[D]、[VH]为该item的实际类型, 但应该是[BaseMultiRecyclerViewAdapter]的泛型的子类或者实现
 */
interface IRecyclerItem<D, VH : RecyclerView.ViewHolder> {
    val type: Int
    val vhProvider: VHProvider<VH>
    fun onBind(holder: VH, bean: D)
}

abstract class RecyclerItem<D, VH : RecyclerView.ViewHolder>(
    override val type: Int, override val vhProvider: VHProvider<VH>
) : IRecyclerItem<D, VH>
