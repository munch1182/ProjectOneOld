package com.munch.project.testsimple.net

import android.os.Bundle
import android.widget.Button
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R

/**
 * Create by munch1182 on 2021/2/1 15:53.
 */
class TestAndServiceActivity : TestBaseTopActivity() {

    private val start: Button by lazy { findViewById(R.id.service_btn_start) }
    private val stop: Button by lazy { findViewById(R.id.service_btn_stop) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_simple_activity_test_and_service)
        start.setOnClickListener { AndServiceHelper.INSTANCE.startWebService() }
        stop.setOnClickListener { AndServiceHelper.INSTANCE.stopWebService() }
    }

}