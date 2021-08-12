package com.munch.project.one.dev.test

import com.munch.lib.base.startActivity
import com.munch.lib.fast.base.BaseBtnFlowActivity

/**
 * Create by munch1182 on 2021/8/12 15:27.
 */
class TestActivity : BaseBtnFlowActivity() {

    override fun getData() =
        mutableListOf("AppBarLayout")

    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            0 -> startActivity(TestAppbarActivity::class.java)
        }
    }
}