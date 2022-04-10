package com.munch.lib.recyclerview

import android.view.View

/**
 * Create by munch1182 on 2022/3/31 14:30.
 */

interface OnItemClickListener<VH : BaseViewHolder> : View.OnClickListener,
    View.OnLongClickListener {

    @Suppress("UNCHECKED_CAST")
    override fun onClick(v: View?) {
        val holder = v?.tag as? VH? ?: return
        onClick(v, holder.bindingAdapterPosition, holder)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onLongClick(v: View?): Boolean {
        val holder = v?.tag as? VH? ?: return false
        return onLongClick(v, holder.bindingAdapterPosition, holder)
    }

    fun onClick(v: View?, pos: Int, holder: VH) {}
    fun onLongClick(v: View?, pos: Int, holder: VH): Boolean = false
}

interface AdapterClickListener<VH : BaseViewHolder> {

    fun setOnItemClickListener(listener: OnItemClickListener<VH>?)

    fun setOnItemLongClickListener(listener: OnItemClickListener<VH>?)

    fun setOnViewClickListener(listener: OnItemClickListener<VH>?, vararg ids: Int)

    fun setOnViewLongClickListener(listener: OnItemClickListener<VH>?, vararg ids: Int)
}

interface AdapterClickHandler<VH : BaseViewHolder> : AdapterClickListener<VH> {

    var itemClickListener: OnItemClickListener<VH>?
    var itemLongClickListener: OnItemClickListener<VH>?

    var clickIds: IntArray
    var longClickIds: IntArray
    var viewClickListener: OnItemClickListener<VH>?
    var viewLongClickListener: OnItemClickListener<VH>?

    override fun setOnItemClickListener(listener: OnItemClickListener<VH>?) {
        itemClickListener = listener
    }

    override fun setOnItemLongClickListener(listener: OnItemClickListener<VH>?) {
        itemLongClickListener = listener
    }

    override fun setOnViewClickListener(listener: OnItemClickListener<VH>?, vararg ids: Int) {
        viewClickListener = listener
        clickIds = ids
    }

    override fun setOnViewLongClickListener(listener: OnItemClickListener<VH>?, vararg ids: Int) {
        viewLongClickListener = listener
        longClickIds = ids
    }
}

class AdapterListenerHelper<VH : BaseViewHolder> : AdapterClickHandler<VH> {

    override var itemClickListener: OnItemClickListener<VH>? = null
    override var itemLongClickListener: OnItemClickListener<VH>? = null

    override var clickIds: IntArray = intArrayOf()
    override var longClickIds: IntArray = intArrayOf()
    override var viewClickListener: OnItemClickListener<VH>? = null
    override var viewLongClickListener: OnItemClickListener<VH>? = null

}