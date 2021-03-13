package com.munch.project.test.dialog

import android.os.Bundle
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.test.R
import com.munch.project.test.databinding.TestActivityTestDialogBinding

/**
 * Create by munch1182 on 2021/3/12 10:39.
 */
class TestDialogActivity : TestBaseTopActivity() {

    private val bind by bindingTop<TestActivityTestDialogBinding>(R.layout.test_activity_test_dialog)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        bind.testDialogStart.setOnClickListener { start() }
    }

    private fun start() {

    }
}