package com.munch.test.toolbar

import android.os.Bundle
import com.munch.lib.libnative.helper.PhoneHelper
import com.munch.lib.libnative.helper.ViewHelper
import com.munch.test.R
import com.munch.test.base.BaseActivity
import kotlinx.android.synthetic.main.activity_test_tool_bar2.*

/**
 * Create by Munch on 2020/09/02
 */
class TestToolBar2Activity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_tool_bar2)

        setStatusBar()

        setToolBar(toolbar_tb2)

        val params = ViewHelper.getParams(toolbar_tb2)
        params.height = PhoneHelper.getActionBarHeight() + PhoneHelper.getStatusBarHeight()
        toolbar_tb2.requestLayout()
        ViewHelper.setViewPadding(toolbar_tb2, top = PhoneHelper.getStatusBarHeight())
    }
}