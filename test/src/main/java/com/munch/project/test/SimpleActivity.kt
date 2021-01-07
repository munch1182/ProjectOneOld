package com.munch.project.test

import android.os.Bundle
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.test.bar.TestBarActivity
import com.munch.project.test.file.TestFileActivity
import com.munch.project.test.view.TestChartActivity
import com.munch.project.test.view.TestFlowLayoutActivity
import com.munch.project.test.view.TestRecyclerViewActivity
import com.munch.project.test.view.TestWeightActivity

/**
 * 一些简单逻辑用于过渡的activity
 * Create by munch1182 on 2020/12/10 21:33.
 */
class TestViewActivity : TestRvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBack()
    }

    override fun getItems(): MutableList<TestRvItemBean> {
        return mutableListOf(
            TestRvItemBean.newInstance("Weight", TestWeightActivity::class.java),
            TestRvItemBean.newInstance("FlowLayout", TestFlowLayoutActivity::class.java),
            TestRvItemBean.newInstance("Bar", TestBarActivity::class.java),
            TestRvItemBean.newInstance("RecyclerView", TestRecyclerViewActivity::class.java),
            TestRvItemBean.newInstance("Chart", TestChartActivity::class.java)
        )
    }
}

class TestFileMainActivity : TestRvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBack()
    }

    override fun getItems(): MutableList<TestRvItemBean> {
        return mutableListOf(
            TestRvItemBean.newInstance("All File", TestFileActivity::class.java)
        )
    }
}