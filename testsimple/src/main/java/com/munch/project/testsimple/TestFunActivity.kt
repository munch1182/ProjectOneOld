package com.munch.project.testsimple

import android.content.Intent
import android.view.View
import com.munch.lib.log
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean

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
               log("121312312314134234242sdfsdfwefsdfsfsdfsdfsdfsdfsgssgsfgs322fsfd2232dsfsdf22dfsdfsf2f2e2fs")
            }
            1 -> {
                log("123456".substring(0,3))
            }
            2 -> {
            }
            3 -> {
            }
        }
    }

    override fun getItems(): MutableList<TestRvItemBean> {
        return TestRvItemBean.newArray("test1", "test2", "test3", "test4")
    }
}