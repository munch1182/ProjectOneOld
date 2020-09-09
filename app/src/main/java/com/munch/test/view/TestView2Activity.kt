package com.munch.test.view

import android.os.Bundle
import android.view.View
import com.munch.test.R
import com.munch.test.base.BaseActivity
import com.munch.test.view.weight.FlowLayout
import kotlinx.android.synthetic.main.activity_test_view2.*


/**
 * Create by Munch on 2020/09/04
 */
class TestView2Activity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_view2)
        setToolBar(view_tb, "View")


        val listener = View.OnClickListener {
            val gravity = when (it.tag.toString().toInt()) {
                0 -> FlowLayout.START
                1 -> FlowLayout.END
                2 -> FlowLayout.CENTER_HORIZONTAL
                3 -> FlowLayout.CENTER_VERTICAL
                4 -> FlowLayout.CENTER
                5 -> FlowLayout.END_CENTER_VERTICAL
                else -> FlowLayout.START
            }
            view_flow.setGravity(gravity)
        }
        view_b1.setOnClickListener(listener)
        view_b2.setOnClickListener(listener)
        view_b3.setOnClickListener(listener)
        view_b4.setOnClickListener(listener)
        view_b5.setOnClickListener(listener)
        view_b6.setOnClickListener(listener)

    }
}