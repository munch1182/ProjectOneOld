package com.munch.lib.fast.recyclerview

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.munch.lib.recyclerview.BaseRecyclerViewAdapter

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