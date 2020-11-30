package com.munch.test.service

import android.os.Bundle
import com.munch.test.base.RvActivity
import kotlinx.android.synthetic.main.activity_rv.*

/**
 * Create by Munch on 2020/09/04
 */
class TestServiceActivity : RvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.setOnItemClickListener { _, _, position ->
            when (position) {
                0 -> startActivity(TestService1Activity::class.java)
                else -> return@setOnItemClickListener
            }
        }

        setToolBar(rv_tb, "View")
    }

    override fun addItemList(list: ArrayList<String>) {
        super.addItemList(list)
        list.add("service")
    }
}