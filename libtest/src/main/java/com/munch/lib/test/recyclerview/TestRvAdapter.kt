package com.munch.lib.test.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.helper.startActivity
import com.munch.lib.test.R

/**
 * 将两个并不相等的功能合并进一个类徒增难度
 * 但test只是省事
 *
 * Create by munch1182 on 2020/12/7 13:58.
 */
class TestRvAdapter(items: MutableList<TestRvItemBean>?, private val isBtn: Boolean = false) :
    RecyclerView.Adapter<TestRvAdapter.ViewHolder>() {

    constructor(isBtn: Boolean = false) : this(mutableListOf(), isBtn)

    private val items: MutableList<TestRvItemBean> = mutableListOf()
    private var listener: View.OnClickListener? = null

    init {
        if (items != null) {
            this.items.addAll(items)
        }
    }

    fun getData() = items

    fun setData(data: MutableList<TestRvItemBean>) {
        items.clear()
        this.items.addAll(data)
        notifyDataSetChanged()
    }

    /**
     * isBtn有效
     */
    fun notifyItem(pos: Int, string: String?) {
        items[pos].info = string ?: ""
        notifyItemChanged(pos)
    }

    /**
     * isBtn有效
     */
    fun clearItemInfo(pos: Int) = notifyItem(pos, null)

    /**
     * isBtn有效
     */
    fun clearItemInfo() {
        setData(items.map {
            it.info = ""
            it
        }.toMutableList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                if (isBtn) R.layout.item_rv_test_btn else R.layout.item_rv_test, parent, false
            ).apply {
                if (isBtn) {
                    setPadding(0, 0, 0, 0)
                }
            }
        ).apply {
            if (this@TestRvAdapter.listener != null) {
                itemView.findViewById<TextView>(R.id.item_rv_test_tv).setOnClickListener {
                    it.tag = absoluteAdapterPosition
                    listener!!.onClick(it)
                }
            } else {
                itemView.setOnClickListener {
                    it.context.startActivity(
                        items[absoluteAdapterPosition].getTarget() ?: return@setOnClickListener
                    )
                }
            }
        }
    }

    fun clickItemListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.item_rv_test_tv).text = items[position].name
        if (isBtn) {
            holder.itemView.findViewById<TextView>(R.id.item_rv_test_tv2).text =
                items[position].info
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}