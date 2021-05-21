package com.munch.pre.lib.base.rv

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

interface ItemClickListener<D, V : BaseViewHolder> : View.OnClickListener,
    View.OnLongClickListener {

    fun onItemClick(adapter: BaseAdapter<D, V>, bean: D, view: View, pos: Int)

    override fun onClick(v: View?) {
        handleClick(v)
    }

    @Suppress("UNCHECKED_CAST")
    fun handleClick(v: View?): Boolean {
        v ?: return false
        val holder = v.tag as? BaseViewHolder ?: return false
        val position = holder.bindingAdapterPosition
        if (position == RecyclerView.NO_POSITION) {
            return false
        }
        val adapter = holder.bindingAdapter?.takeIf { it is BaseAdapter<*, *> } ?: return false
        onItemClick(adapter as BaseAdapter<D, V>, adapter.getData()[position], v, position)
        return true
    }

    override fun onLongClick(v: View?): Boolean {
        return handleClick(v)
    }
}

open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun <D, V : BaseViewHolder> setOnItemClickListener(itemClick: ItemClickListener<D, V>?) {
        itemView.tag = this
        itemView.setOnClickListener(itemClick)
    }

    fun <D, V : BaseViewHolder> setOnItemLongClickListener(longClick: ItemClickListener<D, V>?) {
        itemView.tag = this
        itemView.setOnLongClickListener(longClick)
    }

}

class DiffUtilCallBack<D>(vararg parameter: (D) -> Any) : DiffUtil.ItemCallback<D>() {

    private val diff = parameter

    /**
     * 决定两个数据是否是同一个
     */
    override fun areItemsTheSame(oldItem: D, newItem: D): Boolean {
        diff.forEach {
            if (it.invoke(oldItem) != it.invoke(newItem)) {
                return false
            }
        }
        return true
    }

    /**
     * 当两个数据是同一个时，是否要进行更新
     */
    override fun areContentsTheSame(oldItem: D, newItem: D): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }
}