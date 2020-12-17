package com.munch.project.test

import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.test.camera.TestCameraActivity

/**
 * Create by munch1182 on 2020/12/7 13:58.
 */
class TestMainActivity : TestRvActivity() {

    override fun notShowBack() = true

    override fun getItems(): MutableList<TestRvItemBean> {
        return mutableListOf(
            TestRvItemBean.newInstance("Camera", TestCameraActivity::class.java),
            TestRvItemBean.newInstance("View", TestViewActivity::class.java)
        )
    }
}