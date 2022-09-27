package com.munch.lib.android.recyclerview

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.core.util.keyIterator
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.android.R
import androidx.recyclerview.widget.RecyclerView.ViewHolder as BaseViewHolder

class ClickHelper<VH : BaseViewHolder> : AdapterEventHelper<VH>,
    View.OnClickListener,
    View.OnLongClickListener {

    private val tagVH = R.id.id_vh

    override var itemClick: OnItemClickListener<VH>? = null
    override var itemLongClick: OnItemLongClickListener<VH>? = null
    override var viewClick: SparseArray<OnItemViewClickListener<VH>?>? = null
    override var viewLongClick: SparseArray<OnItemViewLongClickListener<VH>?>? = null

    override fun onCreate(vh: VH) {
        vh.itemView.setTag(tagVH, vh)
        itemClick?.let { vh.itemView.setOnClickListener(this) }
        itemLongClick?.let { vh.itemView.setOnLongClickListener(this) }
        viewClick?.keyIterator()?.forEach { vh.get<View>(it)?.setOnClickListener(this) }
        viewLongClick?.keyIterator()?.forEach { vh.get<View>(it)?.setOnLongClickListener(this) }
    }

    private fun <V : View> BaseViewHolder.get(viewId: Int): V? = itemView.findViewById(viewId)

    override fun onBind(vh: VH) {
        vh.itemView.setTag(tagVH, vh)
    }

    override fun onClick(view: View?) {
        val vh = view?.let { findTagVH(it) } ?: return
        if (view === vh.itemView) {
            itemClick?.onItemClick(vh)
        } // 同时给itemView设置itemClick和ViewClick会收到两次回调
        if (view.id != View.NO_ID) {
            viewClick?.get(view.id)?.onItemClick(view, vh)
        }
    }

    override fun onLongClick(view: View?): Boolean {
        val vh = view?.let { findTagVH(it) } ?: return false
        if (view === vh.itemView) {
            itemLongClick?.onItemLongClick(vh)
        } // 同时给itemView设置itemLongClick和ViewLongClick会收到两次回调
        if (view.id != View.NO_ID) {
            viewLongClick?.get(view.id)?.onItemLongClick(view, vh)
        }
        return true
    }

    @Suppress("UNCHECKED_CAST")
    private fun findTagVH(view: View?): VH? {
        view ?: return null
        if (view is RecyclerView) return null // 向上直到找到RV为止
        val tag = view.getTag(tagVH) ?: return findTagVH(view.parent as? ViewGroup)
        return tag as? VH
    }
}