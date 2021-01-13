package com.munch.project.test.view

import android.os.Bundle
import com.munch.lib.test.BaseActivity

/**
 * Create by munch1182 on 2021/1/9 15:29.
 */
class TestBookPageViewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(BookPageStructureView(this).apply {
            setOnClickListener {
                (it as BookPageStructureView).drawStructure()
            }
        })
    }
}