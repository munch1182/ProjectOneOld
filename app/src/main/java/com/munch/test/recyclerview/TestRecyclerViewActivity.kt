package com.munch.test.recyclerview

import android.os.Bundle
import com.munch.test.base.RvActivity
import kotlinx.android.synthetic.main.activity_rv.*

/**
 * Create by Munch on 2020/09/04
 */
class TestRecyclerViewActivity : RvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.setOnItemClickListener { _, _, position ->
            when (position) {
                0 -> startActivity(TestRv1Activity::class.java)
                1 -> startActivity(TestRv2Activity::class.java)
                else -> return@setOnItemClickListener
            }
        }

        setToolBar(rv_tb, "View")
    }

    override fun addItemList(list: ArrayList<String>) {
        super.addItemList(list)
        list.add("滑动顺序的rv")
        list.add("不规则的rv")
    }
}