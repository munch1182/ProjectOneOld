package com.munch.project.one.load

import android.os.Bundle
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.base.get

/**
 * Create by munch1182 on 2021/9/16 11:20.
 */
class TestDataLoadActivity : BaseBigTextTitleActivity() {

    private val vm by get(DataLadViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}