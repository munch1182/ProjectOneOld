package com.munch.pre.lib.base.rv

import android.view.View
import androidx.annotation.LayoutRes

/**
 * Create by munch1182 on 2021/3/31 15:20.
 */
class SimpleAdapter<D> private constructor(
    @LayoutRes itemRes: Int = 0,
    itemView: View? = null,
    dataInit: MutableList<D>? = null,
    private val onBind: ((holder: BaseViewHolder, bean: D, pos: Int) -> Unit)? = null
) : BaseAdapter<D, BaseViewHolder>(itemRes, itemView, dataInit, null) {

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
}