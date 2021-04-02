package com.munch.lib.fast.base.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.munch.lib.fast.R
import com.munch.lib.fast.base.BaseBindAdapter
import com.munch.lib.fast.base.BaseBindViewHolder
import com.munch.lib.fast.databinding.ActivityBaseRvBinding
import com.munch.lib.fast.databinding.ItemBaseTopBtBinding

/**
 * Create by munch1182 on 2021/3/31 15:35.
 */
abstract class BaseItemActivity : BaseTopActivity() {

    protected open val bind by bind<ActivityBaseRvBinding>(R.layout.activity_base_rv)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setItem(bind.baseTopRv)
        bind.baseTopSrl.run {
            setOnRefreshListener { refresh() }
        }
    }

    protected open fun SwipeRefreshLayout.refresh() {
        postDelayed({ this.isRefreshing = false }, 300L)
    }

    protected open fun setItem(target: RecyclerView) {
        target.run {
            layoutManager = LinearLayoutManager(this@BaseItemActivity)
            adapter = object : BaseBindAdapter<String, ItemBaseTopBtBinding>(
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
        }
    }

    abstract fun clickItem(pos: Int)

    abstract fun getItem(): MutableList<String>
}