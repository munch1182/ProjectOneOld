package com.munch.project.testsimple

import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.testsimple.jetpack.TestPagingActivity

/**
 * Create by munch1182 on 2020/12/17 15:29.
 */

class TestJetpackActivity : TestRvActivity() {

    override fun getItems(): MutableList<TestRvItemBean>? {
        return TestRvItemBean.newArray(TestPagingActivity::class.java)
    }
}