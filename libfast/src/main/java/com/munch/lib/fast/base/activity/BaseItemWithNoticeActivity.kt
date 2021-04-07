package com.munch.lib.fast.base.activity

import android.widget.TextView
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.fast.R
import com.munch.lib.fast.base.BaseBindAdapter
import com.munch.lib.fast.base.BaseBindViewHolder
import com.munch.lib.fast.databinding.ItemBaseTopBtBinding
import com.munch.pre.lib.base.rv.SimpleAdapter

/**
 * Create by munch1182 on 2021/3/31 15:35.
 */
abstract class BaseItemWithNoticeActivity : BaseItemActivity() {

    private var noticeAdapter: SimpleAdapter<String>? = null

    override fun setItem(target: RecyclerView) {
        target.run {
            layoutManager = LinearLayoutManager(this@BaseItemWithNoticeActivity)
            val itemAdapter = object : BaseBindAdapter<String, ItemBaseTopBtBinding>(
                R.layout.item_base_top_bt, getItem()
            ) {
                init {
                    setOnItemClickListener { _, _, _, pos ->
                        clickItem(pos)
                    }
                }

                override fun onBindViewHolder(
                    holder: BaseBindViewHolder<ItemBaseTopBtBinding>,
                    bean: String,
                    pos: Int
                ) {
                    holder.bind.itemBaseTopBt.text = bean
                }
            }
            noticeAdapter =
                SimpleAdapter(R.layout.item_base_top_tv, mutableListOf("")) { holder, bean, _ ->
                    if (holder.itemView is TextView) {
                        (holder.itemView as TextView).text = bean
                    }
                }
            val concatAdapter = ConcatAdapter(itemAdapter, noticeAdapter)
            adapter = concatAdapter
        }
    }

    fun notice(notice: String) {
        runOnUiThread {
            noticeAdapter?.set(0, notice)
        }
    }
}