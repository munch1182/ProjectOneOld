package com.munch.project.testsimple.jetpack

import android.os.Bundle
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R

/**
 * Create by munch1182 on 2020/12/11 17:14.
 */
class TestJetpackActivity : TestBaseTopActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_jet_pack)
    }
}