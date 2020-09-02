package com.munch.test.toolbar

import android.os.Bundle
import com.munch.test.base.RvActivity
import kotlinx.android.synthetic.main.activity_rv.*

/**
 * Create by munch on 2020/09/02
 */
class TestToolBarActivity : RvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter.setOnItemClickListener { _, _, position ->
            when (position) {
                0 -> startActivity(TestToolBar1Activity::class.java)
                1 -> startActivity(TestToolBar2Activity::class.java)
                else -> return@setOnItemClickListener
            }
        }

        setToolBar(rv_tb,"ToolBar")
    }

    override fun addItemList(list: ArrayList<String>) {
        super.addItemList(list)
        list.add("一般使用")
        list.add("头图滑动")
    }
}