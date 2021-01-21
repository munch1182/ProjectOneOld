package com.munch.project.testsimple.com.munch.project.testsimple

import android.os.Bundle
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.testsimple.MainRvActivity
import com.munch.project.testsimple.R
import com.munch.project.testsimple.TestFunActivity
import com.munch.project.testsimple.alive.TestAliveActivity
import com.munch.project.testsimple.jetpack.TestJetpackActivity
import com.munch.project.testsimple.net.TestNetActivity
import com.munch.project.testsimple.queue.TestQueueActivity
import com.munch.project.testsimple.sensor.TestSensorActivity
import com.munch.project.testsimple.net.TestSimpleIpActivity

class MainRvAloneActivity : MainRvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.test_simple_app_name)
        showBack(false)
    }
}