package com.munch.lib.fast.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
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