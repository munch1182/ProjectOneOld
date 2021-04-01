package com.munch.lib.fast.base

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.munch.lib.fast.R
import com.munch.lib.fast.databinding.ActivityBaseRvBinding
import com.munch.lib.fast.databinding.ItemBaseTopTvBinding
import com.munch.lib.fast.helper.RvHelper
import com.munch.pre.lib.extend.startActivity

/**
 * Create by munch1182 on 2021/3/31 15:35.
 */
abstract class BaseRvActivity : BaseTopActivity() {

    private val bind by bind<ActivityBaseRvBinding>(R.layout.activity_base_rv)

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
            layoutManager = LinearLayoutManager(this@BaseRvActivity)
            addItemDecoration(RvHelper.newLineDecoration())
            adapter = object : BaseBindAdapter<ItemClassBean, ItemBaseTopTvBinding>(
                R.layout.item_base_top_tv, getClassItem()
            ) {
                init {
                    setOnItemClickListener { _, bean, _, _ ->
                        if (bean.target != null) {
                            startActivity(bean.target)
                        }
                    }
                }

                override fun onBindViewHolder(
                    holder: BaseBindViewHolder<ItemBaseTopTvBinding>,
                    bean: ItemClassBean,
                    pos: Int
                ) {
                    holder.bind.itemBaseTopTv.text = bean.name
                }

            }
        }
    }

    abstract fun getClassItem(): MutableList<ItemClassBean>

    data class ItemClassBean(val name: String, val target: Class<out Activity>? = null) {

        companion object {

            fun newItem(target: Class<out Activity>): ItemClassBean {
                return ItemClassBean(target.simpleName.replace("Activity", ""), target)
            }

            fun newItems(vararg target: Class<out Activity>): MutableList<ItemClassBean> {
                return target.map { newItem(it) }.toMutableList()
            }
        }
    }
}