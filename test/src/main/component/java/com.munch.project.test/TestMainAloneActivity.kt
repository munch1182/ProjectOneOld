package com.munch.project.test

import android.os.Bundle
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.test.img.TestImgActivity
import com.munch.project.test.switch.TestSwitchActivity

/**
 * Create by munch1182 on 2020/12/7 13:58.
 */
class TestMainAloneActivity : TestRvActivity() {

    override fun notShowBack() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //用于测试首页启动时间，logcat Nofilters查看Fully drawn语句
        /*if (BaseApp.debugMode()) {
            reportFullyDrawn()
        }*/
        setTitle(R.string.test_main_title)
    }

    override fun getItems(): MutableList<TestRvItemBean> {
        return mutableListOf(
            TestRvItemBean.newInstance("Img", TestImgActivity::class.java),
            TestRvItemBean.newInstance("View", TestViewActivity::class.java),
            TestRvItemBean.newInstance("Switch", TestSwitchActivity::class.java),
            TestRvItemBean.newInstance("File", TestFileMainActivity::class.java)
        )
    }
}