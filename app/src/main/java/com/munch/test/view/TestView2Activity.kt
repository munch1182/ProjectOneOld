package com.munch.test.view

import android.os.Bundle
import com.munch.test.R
import com.munch.test.base.BaseActivity
import kotlinx.android.synthetic.main.activity_test_view2.*


/**
 * Create by Munch on 2020/09/04
 */
class TestView2Activity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_view2)
        setToolBar(view_tb, "View")

    }
}