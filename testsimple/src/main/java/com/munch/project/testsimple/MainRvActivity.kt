package com.munch.project.testsimple

import android.os.Bundle
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.testsimple.alive.TestAliveActivity
import com.munch.project.testsimple.jetpack.TestJetpackActivity

class MainRvActivity : TestRvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.app_name)
    }

    override fun getItems(): MutableList<TestRvItemBean> {
        return mutableListOf(
            TestRvItemBean.newInstance("Alive", TestAliveActivity::class.java),
            TestRvItemBean.newInstance("Jetpack", TestJetpackActivity::class.java),
            TestRvItemBean.newInstance("Test", TestFunActivity::class.java),
        )
    }
}