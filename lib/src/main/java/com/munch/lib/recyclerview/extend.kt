package com.munch.lib.recyclerview

import android.os.Handler
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import com.munch.lib.extend.ViewCreator
import com.munch.lib.helper.ThreadHelper

/**
 * Create by munch1182 on 2022/3/31 17:19.
 */

fun <D, VH : BaseViewHolder> BaseRecyclerViewAdapter<D, VH>.setOnItemClickListener(
    onClick: ((v: View?, holder: VH) -> Unit)?
) {
    if (onClick == null) {
        setOnItemClickListener(null)
    } else {
        setOnItemClickListener(object : OnItemClickListener<VH> {
            override fun onClick(v: View?, holder: VH) {
                super.onClick(v, holder)
                onClick.invoke(v, holder)
            }
        })
    }
}

fun <D, VH : BaseViewHolder> BaseRecyclerViewAdapter<D, VH>.setOnItemLongClickListener(
    onLongClick: ((v: View?, holder: VH) -> Boolean)?
) {
    if (onLongClick == null) {
        setOnItemLongClickListener(null)
    } else {
        setOnItemLongClickListener(object : OnItemClickListener<VH> {
            @Suppress("UNCHECKED_CAST")
            override fun onLongClick(v: View?, holder: VH): Boolean {
                return onLongClick.invoke(v, holder)
            }
        })
    }
}

fun <D, VH : BaseViewHolder> BaseRecyclerViewAdapter<D, VH>.setOnViewClickListener(
    onClick: ((v: View?, holder: VH) -> Unit), vararg ids: Int
) {
    setOnViewClickListener(object : OnItemClickListener<VH> {
        @Suppress("UNCHECKED_CAST")
        override fun onClick(v: View?, holder: VH) {
            onClick.invoke(v, holder)
        }
    }, *ids)
}

fun <D, VH : BaseViewHolder> BaseRecyclerViewAdapter<D, VH>.setOnViewLongClickListener(
    onLongClick: ((v: View?, holder: VH) -> Boolean), vararg ids: Int
) {
    setOnViewLongClickListener(object : OnItemClickListener<VH> {
        @Suppress("UNCHECKED_CAST")
        override fun onLongClick(v: View?, holder: VH): Boolean {
            return onLongClick.invoke(v, holder)
        }
    }, *ids)
}

fun <D, VH : BaseViewHolder> AdapterHelper<D, VH>.setOnItemClickListener(
    onClick: ((v: View?, holder: VH) -> Unit)?
) {
    if (onClick == null) {
        setOnItemClickListener(null)
    } else {
        setOnItemClickListener(object : OnItemClickListener<VH> {
            override fun onClick(v: View?, holder: VH) {
                super.onClick(v, holder)
                onClick.invoke(v, holder)
            }
        })
    }
}

fun <D, VH : BaseViewHolder> AdapterHelper<D, VH>.setOnItemLongClickListener(
    onLongClick: ((v: View?, holder: VH) -> Boolean)?
) {
    if (onLongClick == null) {
        setOnItemLongClickListener(null)
    } else {
        setOnItemLongClickListener(object : OnItemClickListener<VH> {
            @Suppress("UNCHECKED_CAST")
            override fun onLongClick(v: View?, holder: VH): Boolean {
                return onLongClick.invoke(v, holder)
            }
        })
    }
}

fun <D, VH : BaseViewHolder> AdapterHelper<D, VH>.setOnViewClickListener(
    onClick: ((v: View?, holder: VH) -> Unit), vararg ids: Int
) {
    setOnViewClickListener(object : OnItemClickListener<VH> {
        @Suppress("UNCHECKED_CAST")
        override fun onClick(v: View?, holder: VH) {
            onClick.invoke(v, holder)
        }
    }, *ids)
}

fun <D, VH : BaseViewHolder> AdapterHelper<D, VH>.setOnViewLongClickListener(
    onLongClick: ((v: View?, holder: VH) -> Boolean), vararg ids: Int
) {
    setOnViewLongClickListener(object : OnItemClickListener<VH> {
        @Suppress("UNCHECKED_CAST")
        override fun onLongClick(v: View?, holder: VH): Boolean {
            return onLongClick.invoke(v, holder)
        }
    }, *ids)
}

abstract class RVAdapter<D>(
    viewImp: VHProvider,
    adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
    clickHelper: AdapterClickHandler<BaseViewHolder> = AdapterListenerHelper(),
) : BaseRecyclerViewAdapter<D, BaseViewHolder>(viewImp, adapterFun, clickHelper) {

    constructor(
        @LayoutRes res: Int = 0,
        adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
        clickHelper: AdapterClickHandler<BaseViewHolder> = AdapterListenerHelper()
    ) : this(SimpleVHProvider(res), adapterFun, clickHelper)

    constructor(
        viewCreator: ViewCreator,
        adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
        clickHelper: AdapterClickHandler<BaseViewHolder> = AdapterListenerHelper()
    ) : this(SimpleVHProvider(viewCreator), adapterFun, clickHelper)
}

abstract class SimpleCallback<T : Any> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }
}

fun <D : Any> differ(
    content: (o: D, n: D) -> Boolean, item: (((o: D, n: D) -> Boolean)?) = null,
    handler: Handler = ThreadHelper.mainHandler
): AdapterFunImp.Differ<D> {
    return AdapterFunImp.Differ(object : DiffUtil.ItemCallback<D>() {
        override fun areItemsTheSame(oldItem: D, newItem: D): Boolean {
            return item?.invoke(oldItem, newItem) ?: (oldItem.hashCode() == newItem.hashCode())
        }

        override fun areContentsTheSame(oldItem: D, newItem: D): Boolean {
            return content.invoke(oldItem, newItem)
        }
    }, handler)
}