package com.munch.project.testsimple

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.munch.lib.common.RouterHelper
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.testsimple.alive.TestAliveActivity
import com.munch.project.testsimple.jetpack.TestJetpackActivity
import com.munch.project.testsimple.net.TestNetActivity
import com.munch.project.testsimple.queue.TestQueueActivity
import com.munch.project.testsimple.sensor.TestSensorActivity

@Route(path = RouterHelper.TestSimple.MAIN)
open class MainRvActivity : TestRvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Test Simple"
        showBack(true)
    }

    override fun getItems(): MutableList<TestRvItemBean> {
        return mutableListOf(
            TestRvItemBean.newInstance("Alive", TestAliveActivity::class.java),
            TestRvItemBean.newInstance("Jetpack", TestJetpackActivity::class.java),
            TestRvItemBean.newInstance("Net", TestNetActivity::class.java),
            TestRvItemBean.newInstance("Queue", TestQueueActivity::class.java),
            TestRvItemBean.newInstance("Sensor", TestSensorActivity::class.java),
            TestRvItemBean.newInstance("Test", TestFunActivity::class.java)
        )
    }
}