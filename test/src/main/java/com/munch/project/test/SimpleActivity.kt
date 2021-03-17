package com.munch.project.test

import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.test.bar.TestBarActivity
import com.munch.project.test.file.TestFileActivity
import com.munch.project.test.view.*

/**
 * 一些简单逻辑用于过渡的activity
 * Create by munch1182 on 2020/12/10 21:33.
 */
class TestViewActivity : TestRvActivity() {
    override fun getItems(): MutableList<TestRvItemBean> {
        return mutableListOf(
            TestRvItemBean.newInstance("Weight", TestWeightActivity::class.java),
            TestRvItemBean.newInstance("Calendar", TestCalendarViewActivity::class.java),
            TestRvItemBean.newInstance("BookPage", TestBookPageViewActivity::class.java),
            TestRvItemBean.newInstance("Fish", TestFishStructureActivity::class.java),
            TestRvItemBean.newInstance("FlowLayout", TestFlowLayoutActivity::class.java),
            TestRvItemBean.newInstance("Bar", TestBarActivity::class.java),
            TestRvItemBean.newInstance("NavRecyclerView", TestNavRecyclerViewActivity::class.java),
            TestRvItemBean.newInstance(
                "HeaderRecyclerView",
                TestHeaderRecyclerViewActivity::class.java
            ),
            TestRvItemBean.newInstance("Chart", TestChartActivity::class.java),
            TestRvItemBean.newInstance("PorterDuffXfermode", PaintModeActivity::class.java)
        )
    }
}

class TestFileMainActivity : TestRvActivity() {
    override fun getItems(): MutableList<TestRvItemBean> {
        return mutableListOf(
            TestRvItemBean.newInstance("App File", TestFileActivity::class.java)
        )
    }
}

class TestNetMainActivity : TestRvActivity(){

}