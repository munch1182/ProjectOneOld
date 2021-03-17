package com.munch.project.test.net

import android.os.Bundle
import android.view.View
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.test.R

/**
 * Create by munch1182 on 2021/3/17 9:16.
 */
class TestNetActivity : TestBaseTopActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity_test_net)
        findViewById<View>(R.id.test_net_start).setOnClickListener {
        }
    }
}