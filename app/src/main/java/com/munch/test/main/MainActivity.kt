package com.munch.test.main

import android.os.Bundle
import com.munch.test.R
import com.munch.test.base.RvActivity
import com.munch.test.toolbar.TestToolBarActivity
import com.munch.test.view.TestViewActivity

/**
 * Create by Munch on 2020/09/02
 */
class MainActivity : RvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter.setOnItemClickListener { _, _, position ->
            when (position) {
                0 -> startActivity(TestToolBarActivity::class.java)
                1 -> startActivity(TestViewActivity::class.java)
                else -> return@setOnItemClickListener
            }
        }

        supportActionBar!!.setTitle(R.string.test_text)
    }

    override fun addItemList(list: ArrayList<String>) {
        super.addItemList(list)
        list.add("toolbar")
        list.add("view")
        list.add("333333333333333")
        list.add("444444444444")
        list.add("555555555555")
    }

}