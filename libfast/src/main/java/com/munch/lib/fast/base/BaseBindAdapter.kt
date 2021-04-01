package com.munch.lib.fast.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.munch.pre.lib.base.rv.BaseAdapter
import com.munch.pre.lib.base.rv.BaseViewHolder

/**
 * Create by munch1182 on 2021/3/31 15:27.
 */
abstract class BaseBindAdapter<D, V : ViewDataBinding>(
    @LayoutRes resId: Int,
    data: MutableList<D>? = null
) : BaseAdapter<D, BaseBindViewHolder<V>>(resId, dataInit = data) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBindViewHolder<V> {
        if (itemRes != 0) {
            val from = LayoutInflater.from(parent.context)
            return BaseBindViewHolder(DataBindingUtil.inflate(from, itemRes, parent, false))
        }
        throw IllegalStateException("cannot create view holder without item layout res")
    }
}

open class BaseBindViewHolder<V : ViewDataBinding>(open val bind: V) : BaseViewHolder(bind.root)