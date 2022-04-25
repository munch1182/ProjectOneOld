package com.munch.project.one

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.base.DataHelper
import com.munch.lib.fast.view.ISupportActionBar
import com.munch.lib.fast.view.fvClassRv
import com.munch.project.one.contentresolver.ContentResolverActivity
import com.munch.project.one.log.LogActivity
import com.munch.project.one.notification.NotificationActivity
import com.munch.project.one.record.RecordActivity
import com.munch.project.one.result.ResultActivity
import com.munch.project.one.task.TaskActivity
import com.munch.project.one.weight.WeightActivity

class MainActivity : BaseFastActivity(), ISupportActionBar {

    private val vb by fvClassRv(
        listOf(
            TaskActivity::class,
            ResultActivity::class,
            ContentResolverActivity::class,
            NotificationActivity::class,
            LogActivity::class,
            RecordActivity::class,
            WeightActivity::class
        )
    )
    override val showHome = false

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataHelper.getStartUp()?.let {
            startActivity(Intent(this, it))
        }
        vb.init()
    }
}