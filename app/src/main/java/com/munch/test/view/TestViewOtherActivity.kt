package com.munch.test.view

import android.os.Bundle
import com.munch.test.R
import com.munch.test.base.BaseActivity
import com.munch.test.view.weight.RulerView
import kotlinx.android.synthetic.main.activity_test_view_other.*

/**
 * Create by Munch on 2020/09/07
 */
class TestViewOtherActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_view_other)
        setToolBar(view_tb, "View")

        view_count.setCount(998)
        view_count.setOnClickListener {
            if (view_cb.isChecked) {
                view_count.countSub()
            } else {
                view_count.countAdd()
            }
        }

        view_ruler_view.target = 45f
        view_ruler_view.setUpdateListener(object : RulerView.UpdateListener {
            override fun update(num: Float) {
                view_ruler_tv.text = num.toString()
            }
        })
    }
}