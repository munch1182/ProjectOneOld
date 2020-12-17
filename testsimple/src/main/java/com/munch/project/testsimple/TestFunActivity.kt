package com.munch.project.testsimple

import android.content.Intent
import android.view.View
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.testsimple.func.TestForegroundService

/**
 * Create by munch1182 on 2020/12/16 17:09.
 */
class TestFunActivity : TestRvActivity() {

    override fun setupIntent() {
        super.setupIntent()
        intent = Intent().putExtras(newBundle("Test Function", null, isBtn = true))
    }

    override fun clickItem(view: View, pos: Int) {
        super.clickItem(view, pos)
        when (pos) {
            0 -> {
                TestForegroundService.start(this)
            }
            1 -> {
            }
            2 -> {
            }
            3 -> {
            }
        }
    }

    override fun getItems(): MutableList<TestRvItemBean>? {
        return TestRvItemBean.newArray("test1", "test2", "test3", "test4")
    }
}