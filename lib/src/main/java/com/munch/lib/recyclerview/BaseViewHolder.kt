package com.munch.lib.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2021/8/5 16:44.
 */
open class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemView.tag = this
        itemView.setOnClickListener(listener)
    }

    fun setOnItemLongClickListener(listener: OnItemClickListener) {
        itemView.tag = this
        itemView.setOnLongClickListener(listener)
    }

    fun setOnViewClickListener(listener: OnItemClickListener, vararg ids: Int) {
        ids.forEach { id ->
            itemView.findViewById<View>(id)?.let {
                it.tag = this
                it.setOnClickListener(listener)
            }
        }
    }

    fun setOnViewLongClickListener(listener: OnItemClickListener, vararg ids: Int) {
        ids.forEach { id ->
            itemView.findViewById<View>(id)?.let {
                it.tag = this
                it.setOnLongClickListener(listener)
            }
        }
    }
}

interface OnItemClickListener : View.OnClickListener, View.OnLongClickListener {

    override fun onClick(v: View?) {
        val holder = v?.tag as? BaseViewHolder? ?: return
        onClick(v, holder)
    }

    override fun onLongClick(v: View?): Boolean {
        val holder = v?.tag as? BaseViewHolder? ?: return false
        return onLongClick(v, holder)
    }

    fun onClick(v: View?, holder: BaseViewHolder)
    fun onLongClick(v: View?, holder: BaseViewHolder): Boolean
}