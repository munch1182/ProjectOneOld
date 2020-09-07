package com.munch.test.view

import android.os.Bundle
import com.munch.test.base.RvActivity
import kotlinx.android.synthetic.main.activity_rv.*

/**
 * Create by Munch on 2020/09/04
 */
class TestViewActivity : RvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.setOnItemClickListener { _, _, position ->
            when (position) {
                0 -> startActivity(TestView1Activity::class.java)
                1 -> startActivity(TestView2Activity::class.java)
                else -> startActivity(TestViewOtherActivity::class.java)
            }
        }

        setToolBar(rv_tb, "View")
    }

    override fun addItemList(list: ArrayList<String>) {
        super.addItemList(list)
        list.add("圆形进度")
        list.add("流式布局")
        list.add("其它")
    }
}