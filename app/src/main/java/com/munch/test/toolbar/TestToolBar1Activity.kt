package com.munch.test.toolbar

import android.os.Bundle
import com.munch.test.R
import com.munch.test.base.BaseActivity
import kotlinx.android.synthetic.main.activity_test_tool_bar1.*

/**
 * Create by Munch on 2020/09/02
 */
class TestToolBar1Activity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_tool_bar1)

        setToolBar(toolbar_tb, "ToolBar")
    }
}