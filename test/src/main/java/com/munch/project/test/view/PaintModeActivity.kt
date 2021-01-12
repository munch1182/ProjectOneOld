package com.munch.project.test.view

import android.os.Bundle
import com.munch.lib.test.TestBaseTopActivity

/**
 * Create by munch1182 on 2021/1/12 17:33.
 */
class PaintModeActivity : TestBaseTopActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(PorterDuffXfermodeView(this))
    }
}