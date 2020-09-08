package com.munch.test.other

import android.os.Bundle
import com.munch.test.base.RvActivity
import kotlinx.android.synthetic.main.activity_rv.*

/**
 * Create by Munch on 2020/09/04
 */
class TestOtherActivity : RvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.setOnItemClickListener { _, _, position ->
            when (position) {
                0 -> startActivity(TestOtherFloatActivity::class.java)
                else -> return@setOnItemClickListener
            }
        }

        setToolBar(rv_tb, "View")
    }

    override fun addItemList(list: ArrayList<String>) {
        super.addItemList(list)
        list.add("Float精度丢失")
    }
}