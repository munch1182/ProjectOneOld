package com.munch.lib.android.recyclerview

import android.view.View

/**
 * Create by munch1182 on 2022/3/31 14:30.
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

interface AdapterClickListener {

    fun setOnItemClickListener(listener: OnItemClickListener?)

    fun setOnItemLongClickListener(listener: OnItemClickListener?)

    fun setOnViewClickListener(listener: OnItemClickListener?, vararg ids: Int)

    fun setOnViewLongClickListener(listener: OnItemClickListener?, vararg ids: Int)
}

interface AdapterClickHandler : AdapterClickListener {

    var itemClickListener: OnItemClickListener?
    var itemLongClickListener: OnItemClickListener?

    var clickIds: IntArray
    var longClickIds: IntArray
    var viewClickListener: OnItemClickListener?
    var viewLongClickListener: OnItemClickListener?

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

class AdapterListenerHelper : AdapterClickHandler {

    override var itemClickListener: OnItemClickListener? = null
    override var itemLongClickListener: OnItemClickListener? = null

    override var clickIds: IntArray = intArrayOf()
    override var longClickIds: IntArray = intArrayOf()
    override var viewClickListener: OnItemClickListener? = null
    override var viewLongClickListener: OnItemClickListener? = null

}