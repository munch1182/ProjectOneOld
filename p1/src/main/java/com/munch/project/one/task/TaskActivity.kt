package com.munch.project.one.task

import android.os.Bundle
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.SupportActionBar
import com.munch.lib.fast.view.SupportConfigDialog
import com.munch.lib.fast.view.fvFv
import com.munch.lib.task.TaskHelper

/**
 * Create by munch1182 on 2022/4/16 20:19.
 */
class TaskActivity : BaseFastActivity(),
    ActivityDispatch by (SupportActionBar + SupportConfigDialog()) {

    private val bind by fvFv(arrayOf("normal task", "", "order task"))
    private val taskHelper = TaskHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.click { _, index ->
            when (index) {
                0 -> {}
                1 -> {}
            }
        }
    }
}