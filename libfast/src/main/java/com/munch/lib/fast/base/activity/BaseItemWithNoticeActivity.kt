package com.munch.lib.fast.base.activity

import android.graphics.Color
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.fast.R
import com.munch.pre.lib.base.rv.SimpleAdapter

/**
 * Create by munch1182 on 2021/3/31 15:35.
 */
abstract class BaseItemWithNoticeActivity : BaseItemActivity() {

    private var noticeAdapter: SimpleAdapter<String>? = null

    override fun setItem(target: RecyclerView) {
        target.run {
            layoutManager = LinearLayoutManager(this@BaseItemWithNoticeActivity)
            noticeAdapter =
                SimpleAdapter(R.layout.item_base_top_tv, mutableListOf("")) { holder, bean, _ ->
                    holder.itemView.findViewById<TextView>(R.id.item_base_top_tv)?.text = bean
                    holder.itemView.findViewById<FrameLayout>(R.id.item_base_top_fl)
                        ?.setBackgroundColor(Color.TRANSPARENT)
                }
            adapter = ConcatAdapter(newBlankAdapter(), newItemAdapter())
        }
    }

    fun notice(notice: String) {
        runOnUiThread { noticeAdapter?.set(0, notice) }
    }
}