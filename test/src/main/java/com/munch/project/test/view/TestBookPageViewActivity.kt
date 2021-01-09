package com.munch.project.test.view

import android.graphics.Color
import android.os.Bundle
import com.munch.lib.helper.getColorCompat
import com.munch.lib.test.TestBaseTopActivity

/**
 * Create by munch1182 on 2021/1/9 15:29.
 */
class TestBookPageViewActivity : TestBaseTopActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(BookPageStructureView(this).apply {
            /*setBackgroundColor(Color.GREEN)*/
        })
    }
}