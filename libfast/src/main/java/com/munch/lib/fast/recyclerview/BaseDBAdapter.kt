package com.munch.lib.fast.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.munch.lib.recyclerview.BaseRecyclerViewAdapter
import com.munch.lib.recyclerview.BaseViewHolder
import com.munch.lib.recyclerview.OnItemClickListener
import com.munch.lib.recyclerview.SingleViewModule

abstract class BaseDBAdapter<D, DB : ViewDataBinding, VH : BaseDBViewHolder>(private val layoutId: Int) :
    BaseRecyclerViewAdapter<D, VH>(), SingleViewModule {

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


fun <D, DB : ViewDataBinding, VH : BaseDBViewHolder> BaseDBAdapter<D, DB, VH>.setOnItemClickListener(
    onClick: ((v: View?, pos: Int, bind: DB) -> Unit)?
) {
    if (onClick == null) {
        setOnItemClickListener(null)
    } else {
        setOnItemClickListener(object : OnItemClickListener {
            @Suppress("UNCHECKED_CAST")
            override fun onClick(v: View?, pos: Int, holder: BaseViewHolder) {
                onClick.invoke(v, pos, (holder as VH).getDB())
            }
        })
    }
}

fun <D, DB : ViewDataBinding, VH : BaseDBViewHolder> BaseDBAdapter<D, DB, VH>.setOnItemLongClickListener(
    onLongClick: ((v: View?, pos: Int, bind: DB) -> Boolean)?
) {
    if (onLongClick == null) {
        setOnItemLongClickListener(null)
    } else {
        setOnItemLongClickListener(object : OnItemClickListener {
            @Suppress("UNCHECKED_CAST")
            override fun onLongClick(v: View?, pos: Int, holder: BaseViewHolder): Boolean {
                return onLongClick.invoke(v, pos, (holder as VH).getDB())
            }
        })
    }
}

fun <D, DB : ViewDataBinding, VH : BaseDBViewHolder> BaseDBAdapter<D, DB, VH>.setOnViewClickListener(
    onClick: ((v: View?, pos: Int, bind: DB) -> Unit)?, vararg ids: Int
) {
    if (onClick == null) {
        setOnViewClickListener(null, ids.toMutableList())
    } else {
        setOnViewClickListener(object : OnItemClickListener {
            @Suppress("UNCHECKED_CAST")
            override fun onClick(v: View?, pos: Int, holder: BaseViewHolder) {
                onClick.invoke(v, pos, (holder as VH).getDB())
            }
        }, ids.toMutableList())
    }
}

fun <D, DB : ViewDataBinding, VH : BaseDBViewHolder> BaseDBAdapter<D, DB, VH>.setOnViewLongClickListener(
    onLongClick: ((v: View?, pos: Int, bind: DB) -> Boolean)?, vararg ids: Int
) {
    if (onLongClick == null) {
        setOnViewLongClickListener(null, ids.toMutableList())
    } else {
        setOnViewLongClickListener(object : OnItemClickListener {
            @Suppress("UNCHECKED_CAST")
            override fun onLongClick(v: View?, pos: Int, holder: BaseViewHolder): Boolean {
                return onLongClick.invoke(v, pos, (holder as VH).getDB())
            }
        }, ids.toMutableList())
    }
}