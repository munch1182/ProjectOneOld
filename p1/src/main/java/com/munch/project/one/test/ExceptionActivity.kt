package com.munch.project.one.test

import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.task.pool
import kotlin.concurrent.thread

/**
 * Create by munch1182 on 2021/11/24 15:36.
 */
class ExceptionActivity : BaseBtnFlowActivity() {

    override fun getData() =
        mutableListOf("exception", "e in thread", "e in pool by execute", "e in pool by submit")

    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            0 -> throw RuntimeException("test")
            1 -> thread { throw RuntimeException("test in thread") }
            2 -> pool(submit = false) { throw RuntimeException("test in pool by execute") }
            3 -> {
                pool(submit = true) { throw RuntimeException("test in pool by submit") }
                toast("不能捕获到崩溃")
            }
        }
    }
}