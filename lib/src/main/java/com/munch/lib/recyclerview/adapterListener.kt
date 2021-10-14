package com.munch.lib.recyclerview

import android.view.View

/**
 * Create by munch1182 on 2021/8/7 21:55.
 */

interface OnItemClickListener : View.OnClickListener, View.OnLongClickListener {

    override fun onClick(v: View?) {
        val holder = v?.tag as? BaseViewHolder? ?: return
        onClick(v, holder.bindingAdapterPosition, holder)
    }

    override fun onLongClick(v: View?): Boolean {
        val holder = v?.tag as? BaseViewHolder? ?: return false
        return onLongClick(v, holder.bindingAdapterPosition, holder)
    }

    fun onClick(v: View?, pos: Int, holder: BaseViewHolder) {}
    fun onLongClick(v: View?, pos: Int, holder: BaseViewHolder): Boolean = false
}

interface AdapterListener {

    fun setOnItemClickListener(listener: OnItemClickListener?)
    fun setOnItemLongClickListener(listener: OnItemClickListener?)
    fun setOnViewClickListener(listener: OnItemClickListener?, vararg ids: Int)
    fun setOnViewLongClickListener(listener: OnItemClickListener?, vararg ids: Int)
}

class AdapterListenerHelper : AdapterListener {

    var itemClickListener: OnItemClickListener? = null
    var itemLongClickListener: OnItemClickListener? = null

    var clickIds: IntArray = intArrayOf()
    var longClickIds: IntArray = intArrayOf()
    var viewClickListener: OnItemClickListener? = null
    var viewLongClickListener: OnItemClickListener? = null

    override fun setOnItemClickListener(listener: OnItemClickListener?) {
        itemClickListener = listener
    }

    override fun setOnItemLongClickListener(listener: OnItemClickListener?) {
        itemLongClickListener = listener
    }

    override fun setOnViewClickListener(listener: OnItemClickListener?, vararg ids: Int) {
        viewClickListener = listener
        clickIds = ids
    }

    override fun setOnViewLongClickListener(listener: OnItemClickListener?, vararg ids: Int) {
        viewLongClickListener = listener
        longClickIds = ids
    }
}