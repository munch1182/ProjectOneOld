package com.munch.lib.fast.base

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.fast.R
import com.munch.lib.fast.databinding.ItemSimpleBtnWithNoticeBinding
import com.munch.lib.fast.recyclerview.SimpleAdapter
import com.munch.lib.fast.recyclerview.setOnItemClickListener
import com.munch.lib.fast.recyclerview.setOnViewClickListener
import com.munch.lib.recyclerview.setOnViewLongClickListener

/**
 * 一个按照RV排列，带有一个文字提示控件的Button的列表界面
 *
 * Create by munch1182 on 2021/8/10 17:00.
 */
open class BaseBtnWithNoticeActivity : BaseBigTextTitleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_rv_only)

        val simpleAdapter =
            SimpleAdapter<String, ItemSimpleBtnWithNoticeBinding>(
                R.layout.item_simple_btn_with_notice, getData()
            ) { _, bind, str -> bind.text = str }
        findViewById<RecyclerView>(R.id.rv_view).apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = simpleAdapter
        }
        simpleAdapter.setOnViewClickListener(
            { _, pos, bind -> onClick(pos, bind) },
            R.id.item_tv_view
        )
    }

    protected open fun onClick(pos: Int, bind: ItemSimpleBtnWithNoticeBinding) {
    }

    protected open fun getData(): MutableList<String>? = null
}