package com.munch.lib.recyclerview

import android.content.Context
import android.view.View
import com.munch.lib.base.ViewCreator

fun MultiViewHelper.setType(getter: (pos: Int) -> Int): MultiViewHelper {
    setType(object : ItemViewTypeGetter {
        override fun getItemViewType(pos: Int): Int {
            return getter.invoke(pos)
        }
    })
    return this
}

fun MultiViewHelper.setTypeView(type: Int, creator: (Context) -> View): MultiViewHelper {
    setTypeView(type, object : ViewCreator {
        override fun create(context: Context): View {
            return creator.invoke(context)
        }
    })
    return this
}

fun SingleViewHelper.setContentView(creator: (Context) -> View) {
    setContentView(object : ViewCreator {
        override fun create(context: Context): View {
            return creator.invoke(context)
        }
    })
}

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
    onLongClick: ((v: View?, pos: Int, holder: VH) -> Boolean)
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