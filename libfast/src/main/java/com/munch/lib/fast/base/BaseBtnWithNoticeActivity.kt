package com.munch.lib.fast.base

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.fast.R
import com.munch.lib.fast.databinding.ItemSimpleBtnWithNoticeBinding
import com.munch.lib.fast.recyclerview.setOnViewClickListener
import com.munch.lib.recyclerview.BaseViewHolder
import com.munch.lib.recyclerview.SimpleAdapter
import com.munch.lib.fast.recyclerview.SimpleDiffAdapter as DBAdapter

/**
 * 一个按照RV排列，带有一个文字提示控件的Button的列表界面
 *
 * Create by munch1182 on 2021/8/10 17:00.
 */
open class BaseBtnWithNoticeActivity : BaseBigTextTitleActivity() {

    private val dp16 by lazy { resources.getDimensionPixelSize(R.dimen.paddingDef) }
    private val header by lazy {
        object : SimpleAdapter<String>({
            TextView(it).apply { setPadding(dp16, dp16 / 2, dp16, dp16 / 2) }
        }) {
            override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                (holder.itemView as? TextView)?.text = data[position] ?: "null"
            }
        }
    }
    private val diffUtil = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
    private val itemAdapter by lazy {
        DBAdapter<String, ItemSimpleBtnWithNoticeBinding>(
            R.layout.item_simple_btn_with_notice,
            diffUtil
        ) { _, bind, str -> bind.text = str }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_rv_only)

        findViewById<RecyclerView>(R.id.rv_view).apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = ConcatAdapter(header, itemAdapter)
        }
        itemAdapter.toBase().setOnViewClickListener(
            { _, pos, bind -> onClick(pos, bind) },
            R.id.item_tv_view
        )
        getData()?.let { set(null, it) }
    }

    protected fun set(header: String?, items: MutableList<String?>? = null) {
        showNotice(header)
        setItem(items)
    }

    protected open fun showNotice(notice: String?) {
        notice ?: return
        if (header.data.isEmpty()) {
            header.set(mutableListOf(notice))
        } else {
            header.update(0, notice)
        }
    }

    protected open fun setItem(items: MutableList<String?>?) {
        itemAdapter.set(items)
    }

    protected open fun onClick(pos: Int, bind: ItemSimpleBtnWithNoticeBinding) {
    }

    protected open fun getData(): MutableList<String?>? = null
}