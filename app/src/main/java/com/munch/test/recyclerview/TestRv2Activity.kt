package com.munch.test.recyclerview

import android.os.Bundle
import android.os.SystemClock
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.munch.test.R
import com.munch.test.base.RvActivity
import kotlinx.android.synthetic.main.activity_rv.*

/**
 * 采用多布局是为了避免单布局更改高度的时候产生的错位等问题，这样做更简单
 * 注意约束布局下如果设置不当rv的高度会产生问题的问题
 *
 * 实际使用中得参考设计来决定加载更多时最后一个的高度
 * Create by Munch on 2020/09/09
 */
class TestRv2Activity : RvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolBar(rv_tb)

    }

    override fun setRv(list: ArrayList<String>) {
        /*super.setRv(list)*/
        rv_rv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = MultiAdapter(list)
        adapter.loadMoreModule.setOnLoadMoreListener {
            if (adapter.data.size >= 120) {
                adapter.loadMoreModule.loadMoreEnd(false)
                return@setOnLoadMoreListener
            }
            Thread {
                SystemClock.sleep(1500)
                runOnUiThread {
                    adapter.addData(getList(adapter.data.size - 1))
                    adapter.loadMoreModule.loadMoreComplete()
                }
            }.start()
        }
        /*adapter.loadMoreModule.isEnableLoadMore = false*/
        rv_rv.adapter = adapter
        adapter.setOnItemClickListener { _, _, _ -> }

    }

    private class MultiAdapter(list: ArrayList<String>?) :
        BaseDelegateMultiAdapter<String, BaseViewHolder>(list), LoadMoreModule {

        companion object {
            const val ITEM_TYPE_SHORT = 0
            const val ITEM_TYPE_NORMAL = 1
        }

        init {
            setMultiTypeDelegate(object : BaseMultiTypeDelegate<String>() {
                override fun getItemType(data: List<String>, position: Int): Int {
                    if (position == 0) {
                        return ITEM_TYPE_SHORT
                    }
                    if (data.isNotEmpty() && position == data.size - 1) {
                        return ITEM_TYPE_SHORT
                    }
                    return ITEM_TYPE_NORMAL
                }
            })
            getMultiTypeDelegate()
                ?.addItemType(ITEM_TYPE_NORMAL, R.layout.item_rv_height1)
                ?.addItemType(ITEM_TYPE_SHORT, R.layout.item_rv_height2)
        }

        override fun convert(holder: BaseViewHolder, item: String) {
            holder.setText(R.id.item_tv, item)
        }

    }

    override fun addItemList(list: ArrayList<String>) {
        super.addItemList(list)
        list.addAll(getList())
    }

    private fun getList(startPos: Int = 0): ArrayList<String> {
        val max = 30
        val list = ArrayList<String>(max)
        for (i in 1..max) {
            list.add((startPos + i).toString())
        }
        return list
    }
}