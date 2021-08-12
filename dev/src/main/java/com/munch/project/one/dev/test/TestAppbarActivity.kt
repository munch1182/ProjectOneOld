package com.munch.project.one.dev.test

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.fast.base.BaseActivity
import com.munch.lib.fast.databinding.ItemSimpleTvBinding
import com.munch.lib.fast.recyclerview.SimpleAdapter
import com.munch.lib.helper.BarHelper
import com.munch.project.one.dev.R
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/8/12 15:09.
 */
class TestAppbarActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_appbar)

        setBar()

        val rv = findViewById<RecyclerView>(R.id.test_appbar_rv_view)
        rv.layoutManager = LinearLayoutManager(this)
        val initData = MutableList(55) { Random.nextInt(10000, 99999).toString() }
        rv.adapter = SimpleAdapter<String, ItemSimpleTvBinding>(R.layout.item_simple_tv, initData)
        { _, db, str -> db.text = str }

        val tb = findViewById<Toolbar>(R.id.test_appbar_toolbar_view)
        tb.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setBar() {
        BarHelper(this).apply {
            colorStatusBar(Color.WHITE)
            setTextColorBlack()
        }
    }
}