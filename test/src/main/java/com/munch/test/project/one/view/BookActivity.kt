package com.munch.test.project.one.view

import android.graphics.Color
import android.os.Bundle
import com.munch.lib.fast.base.activity.BaseActivity
import com.munch.lib.fast.weight.BookPageStructureView
import com.munch.pre.lib.helper.BarHelper

/**
 * Create by munch1182 on 2021/4/14 10:32.
 */
class BookActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BarHelper(this).hideStatusBar(true).colorStatusBar(Color.TRANSPARENT)
        setContentView(BookPageStructureView(this).apply {
            setOnClickListener { (it as BookPageStructureView).drawStructure() }
        })

    }
}