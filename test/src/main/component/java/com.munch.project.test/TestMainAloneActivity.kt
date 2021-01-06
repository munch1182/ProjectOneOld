package com.munch.project.test

import com.alibaba.android.arouter.facade.annotation.Route
import com.munch.lib.common.RouterHelper
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.test.img.TestImgActivity
import com.munch.project.test.switch.TestSwitchActivity

/**
 * Create by munch1182 on 2020/12/7 13:58.
 */
@Route(path = RouterHelper.Test.MAIN)
class TestMainAloneActivity : TestRvActivity() {

    override fun notShowBack() = true

    override fun getItems(): MutableList<TestRvItemBean> {
        return mutableListOf(
            TestRvItemBean.newInstance("Img", TestImgActivity::class.java),
            TestRvItemBean.newInstance("View", TestViewActivity::class.java),
            TestRvItemBean.newInstance("Switch", TestSwitchActivity::class.java)
        )
    }
}