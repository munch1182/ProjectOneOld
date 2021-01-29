package com.munch.lib.extend.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * Create by munch1182 on 2021/1/17 19:49.
 */
open class BaseSimpleBindAdapter<T, B : ViewDataBinding>(
    @LayoutRes resId: Int = 0,
    list: MutableList<T>? = null,
    private val onBind: ((holder: BaseBindViewHolder<B>, data: T, position: Int) -> Unit)? = null
) : BaseAdapter<T, BaseBindViewHolder<B>>(resId, list) {

    constructor(
        @LayoutRes resId: Int = 0,
        onBind: (holder: BaseBindViewHolder<B>, data: T, position: Int) -> Unit
    ) : this(resId, null, onBind)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBindViewHolder<B> {
        if (resId != 0) {
            return BaseBindViewHolder(resId, parent)
        }
        throw Exception("bind adapter need bind layout")
    }

    override fun onBind(holder: BaseBindViewHolder<B>, data: T, position: Int) {
        onBind?.invoke(holder, data, position)
    }

}

open class BaseBindViewHolder<T : ViewDataBinding> constructor(val binding: T) :
    BaseViewHolder(binding.root) {

    constructor(
        @LayoutRes layoutResId: Int,
        parent: ViewGroup
    ) : this(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            layoutResId,
            parent,
            false
        )
    )
}