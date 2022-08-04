package com.munch.project.one

import android.content.Intent
import android.os.Bundle
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.base.DataHelper
import com.munch.lib.fast.view.ISupportActionBar
import com.munch.lib.fast.view.fvClassRv
import com.munch.project.one.about.AboutActivity
import com.munch.project.one.bluetooth.BluetoothActivity
import com.munch.project.one.file.FileActivity
import com.munch.project.one.log.LogActivity
import com.munch.project.one.net.NetActivity
import com.munch.project.one.net.WebActivity
import com.munch.project.one.record.RecordActivity
import com.munch.project.one.result.ResultActivity
import com.munch.project.one.skin.SkinActivity
import com.munch.project.one.task.TaskActivity
import com.munch.project.one.weight.WeightActivity

class MainActivity : BaseFastActivity(), ISupportActionBar {

    private val vb by fvClassRv(
        listOf(
            SkinActivity::class,
            BluetoothActivity::class,
            TaskActivity::class,
            ResultActivity::class,
            FileActivity::class,
            NetActivity::class,
            WebActivity::class,
            LogActivity::class,
            RecordActivity::class,
            WeightActivity::class,
            AboutActivity::class,
        )
    )
    override val showHome = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb.init()
        DataHelper.startUp?.let { startActivity(Intent(this, it)) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}