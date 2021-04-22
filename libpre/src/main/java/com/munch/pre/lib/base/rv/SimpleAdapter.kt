package com.munch.pre.lib.base.rv

import android.util.SparseArray
import android.util.SparseIntArray
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * Create by munch1182 on 2021/3/31 15:20.
 */
class SimpleAdapter<D> private constructor(
    override var res: Int,
    override var view: View?,
    dataInit: MutableList<D>? = null,
    private val onBind: ((holder: BaseViewHolder, bean: D, pos: Int) -> Unit)? = null,
) : BaseAdapter<D, BaseViewHolder>(dataInit), SingleType<BaseViewHolder> {

    constructor(
        @LayoutRes itemRes: Int,
        dataInit: MutableList<D>? = null,
        onBind: ((holder: BaseViewHolder, bean: D, pos: Int) -> Unit)? = null
    ) : this(itemRes, null, dataInit, onBind)

    constructor(
        itemView: View,
        dataInit: MutableList<D>? = null,
        onBind: ((holder: BaseViewHolder, bean: D, pos: Int) -> Unit)? = null
    ) : this(0, itemView, dataInit, onBind)

    override fun onBindViewHolder(holder: BaseViewHolder, bean: D, pos: Int) {
        onBind?.invoke(holder, bean, pos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return createVH(parent, viewType)
    }
}

class SimpleMultiAdapter<D> private constructor(
    dataInit: MutableList<D>? = null,
    private val onBind: ((holder: BaseViewHolder, bean: D, pos: Int) -> Unit)? = null,
) : BaseAdapter<D, BaseViewHolder>(dataInit), MultiType<BaseViewHolder> {

    override fun onBindViewHolder(holder: BaseViewHolder, bean: D, pos: Int) {
        onBind?.invoke(holder, bean, pos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return createVH(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return posType?.getItemTypeByPos(position) ?: super.getItemViewType(position)
    }

    override var viewResMap: SparseIntArray? = null
    override var viewMap: SparseArray<View>? = null
    override var posType: MultiTypeWithPos? = null
}