package com.munch.project.one

import android.os.Bundle
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.SupportActionBar
import com.munch.lib.fast.view.fvClassRv
import com.munch.project.one.contentresolver.ContentResolverActivity
import com.munch.project.one.task.TaskActivity

class MainActivity : BaseFastActivity(), SupportActionBar {

    private val vb by fvClassRv(listOf(TaskActivity::class, ContentResolverActivity::class))
    override val showHome = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb.init()
    }
}