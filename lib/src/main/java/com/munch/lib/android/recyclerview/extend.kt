package com.munch.lib.android.recyclerview

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2022/3/31 17:19.
 */

fun <D, VH : BaseViewHolder> BaseRecyclerViewAdapter<D, VH>.setOnItemClickListener(
    onClick: ((v: View?, pos: Int, holder: VH) -> Unit)?
) {
    if (onClick == null) {
        setOnItemClickListener(null)
    } else {
        setOnItemClickListener(object : OnItemClickListener {
            @Suppress("UNCHECKED_CAST")
            override fun onClick(v: View?, pos: Int, holder: BaseViewHolder) {
                onClick.invoke(v, pos, holder as VH)
            }
        })
    }
}

fun <D, VH : BaseViewHolder> BaseRecyclerViewAdapter<D, VH>.setOnItemLongClickListener(
    onLongClick: ((v: View?, pos: Int, holder: VH) -> Boolean)?
) {
    if (onLongClick == null) {
        setOnItemLongClickListener(null)
    } else {
        setOnItemLongClickListener(object : OnItemClickListener {
            @Suppress("UNCHECKED_CAST")
            override fun onLongClick(v: View?, pos: Int, holder: BaseViewHolder): Boolean {
                return onLongClick.invoke(v, pos, holder as VH)
            }
        })
    }
}

fun <D, VH : BaseViewHolder> BaseRecyclerViewAdapter<D, VH>.setOnViewClickListener(
    onClick: ((v: View?, pos: Int, holder: VH) -> Unit), vararg ids: Int
) {
    setOnViewClickListener(object : OnItemClickListener {
        @Suppress("UNCHECKED_CAST")
        override fun onClick(v: View?, pos: Int, holder: BaseViewHolder) {
            onClick.invoke(v, pos, holder as VH)
        }
    }, *ids)
}

fun <D, VH : BaseViewHolder> BaseRecyclerViewAdapter<D, VH>.setOnViewLongClickListener(
    onLongClick: ((v: View?, pos: Int, holder: VH) -> Boolean), vararg ids: Int
) {
    setOnViewLongClickListener(object : OnItemClickListener {
        @Suppress("UNCHECKED_CAST")
        override fun onLongClick(v: View?, pos: Int, holder: BaseViewHolder): Boolean {
            return onLongClick.invoke(v, pos, holder as VH)
        }
    }, *ids)
}