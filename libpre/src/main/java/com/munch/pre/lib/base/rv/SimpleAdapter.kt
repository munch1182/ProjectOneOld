package com.munch.pre.lib.base.rv

import androidx.annotation.LayoutRes

/**
 * Create by munch1182 on 2021/3/31 15:20.
 */
class SimpleAdapter<D>(
    @LayoutRes itemRes: Int = 0,
    private val onBind: ((holder: BaseViewHolder, bean: D, pos: Int) -> Unit)? = null
) : BaseAdapter<D, BaseViewHolder>(itemRes, null, null, null) {
    override fun onBindViewHolder(holder: BaseViewHolder, bean: D, pos: Int) {
        onBind?.invoke(holder, bean, pos)
    }
}