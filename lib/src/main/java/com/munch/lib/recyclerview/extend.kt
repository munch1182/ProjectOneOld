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
    onClick: ((v: View?, pos: Int, holder: BaseViewHolder) -> Unit)?
) {
    if (onClick == null) {
        setOnItemClickListener(null)
    } else {
        setOnItemClickListener(object : OnItemClickListener {
            override fun onClick(v: View?, pos: Int, holder: BaseViewHolder) {
                onClick.invoke(v, pos, holder)
            }

            override fun onLongClick(v: View?, pos: Int, holder: BaseViewHolder): Boolean {
                return false
            }
        })
    }
}

fun <D, VH : BaseViewHolder> BaseRecyclerViewAdapter<D, VH>.setOnItemLongClickListener(
    onLongClick: ((v: View?, pos: Int, holder: BaseViewHolder) -> Boolean)?
) {
    if (onLongClick == null) {
        setOnItemLongClickListener(null)
    } else {
        setOnItemLongClickListener(object : OnItemClickListener {
            override fun onClick(v: View?, pos: Int, holder: BaseViewHolder) {

            }

            override fun onLongClick(v: View?, pos: Int, holder: BaseViewHolder): Boolean {
                return onLongClick.invoke(v, pos, holder)
            }
        })
    }
}

fun <D, VH : BaseViewHolder> BaseRecyclerViewAdapter<D, VH>.setOnViewClickListener(
    onClick: ((v: View?, pos: Int, holder: BaseViewHolder) -> Unit)?, vararg ids: Int
) {
    if (onClick == null) {
        setOnViewClickListener(null, ids.toMutableList())
    } else {
        setOnViewClickListener(object : OnItemClickListener {
            override fun onClick(v: View?, pos: Int, holder: BaseViewHolder) {
                onClick.invoke(v, pos, holder)
            }

            override fun onLongClick(v: View?, pos: Int, holder: BaseViewHolder): Boolean {
                return false
            }
        }, ids.toMutableList())
    }
}

fun <D, VH : BaseViewHolder> BaseRecyclerViewAdapter<D, VH>.setOnViewLongClickListener(
    onLongClick: ((v: View?, pos: Int, holder: BaseViewHolder) -> Boolean)?, vararg ids: Int
) {
    if (onLongClick == null) {
        setOnViewLongClickListener(null, ids.toMutableList())
    } else {
        setOnViewLongClickListener(object : OnItemClickListener {
            override fun onClick(v: View?, pos: Int, holder: BaseViewHolder) {

            }

            override fun onLongClick(v: View?, pos: Int, holder: BaseViewHolder): Boolean {
                return onLongClick.invoke(v, pos, holder)
            }
        }, ids.toMutableList())
    }
}