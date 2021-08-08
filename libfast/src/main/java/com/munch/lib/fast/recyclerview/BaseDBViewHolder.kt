package com.munch.lib.fast.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.munch.lib.recyclerview.BaseRecyclerViewAdapter
import com.munch.lib.recyclerview.BaseViewHolder

/**
 * Create by munch1182 on 2021/8/8 1:28.
 */
open class BaseDBViewHolder(private val bind: ViewDataBinding) : BaseViewHolder(bind.root) {

    constructor(@LayoutRes layoutId: Int, parent: ViewGroup) : this(
        DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutId, parent, false)
    )

    @Suppress("UNCHECKED_CAST")
    fun <DB : ViewDataBinding> getDB() = bind as DB
}

abstract class BaseDBAdapter<D, DB : ViewDataBinding, VH : BaseDBViewHolder>(private val layoutId: Int) :
    BaseRecyclerViewAdapter<D, VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        @Suppress("UNCHECKED_CAST")
        return BaseDBViewHolder(layoutId, parent) as VH
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        super.onBindViewHolder(holder, position)
        onBindViewHolder(holder, holder.getDB(), data[position])
    }

    abstract fun onBindViewHolder(holder: VH, db: DB, bean: D?)
}