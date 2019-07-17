package com.munch.module.bluetooth

import android.content.Intent
import android.os.Bundle
import com.munch.lib.result.ResultHelper
import com.munch.lib.result.ResultListener
import com.munch.lib.test.TestBaseActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by Munch on 2019/7/16 8:46
 */
class MainActivity : TestBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ble_btn_connect.setOnClickListener {
            ResultHelper.start4Result(this, BleScanListActivity::class.java, 123)
                .result(object : ResultListener {
                    override fun result(resultCode: Int, intent: Intent) {
                    }
                })
        }
    }
}