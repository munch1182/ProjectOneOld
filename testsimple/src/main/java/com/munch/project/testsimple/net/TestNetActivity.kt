package com.munch.project.testsimple.net

import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean

/**
 * Create by munch1182 on 2021/1/21 14:35.
 */
class TestNetActivity : TestRvActivity() {

    override fun getItems(): MutableList<TestRvItemBean> {
        return mutableListOf(
            TestRvItemBean.newInstance("IpAddress", TestSimpleIpActivity::class.java),
            TestRvItemBean.newInstance("Socket Base", TestSocketBaseActivity::class.java),
            TestRvItemBean.newInstance("Socket", TestSocketBroadcastAndConnectActivity::class.java),
            TestRvItemBean.newInstance("Clip", TestClipActivity::class.java)
        )
    }
}