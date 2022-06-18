package com.munch.project.one

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.munch.lib.extend.color
import com.munch.lib.extend.getAttrArrayFromTheme
import com.munch.lib.extend.getColorPrimary
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.base.DataHelper
import com.munch.lib.fast.view.ISupportActionBar
import com.munch.lib.fast.view.fvClassRv
import com.munch.lib.helper.BarHelper
import com.munch.lib.helper.SkinHelper
import com.munch.project.one.about.AboutActivity
import com.munch.project.one.bluetooth.BluetoothActivity
import com.munch.project.one.dialog.DialogActivity
import com.munch.project.one.file.FileActivity
import com.munch.project.one.log.LogActivity
import com.munch.project.one.net.NetActivity
import com.munch.project.one.record.RecordActivity
import com.munch.project.one.result.ResultActivity
import com.munch.project.one.skin.SkinActivity
import com.munch.project.one.task.TaskActivity
import com.munch.project.one.weight.WeightActivity

class MainActivity : BaseFastActivity(), ISupportActionBar {

    private val skin by lazy { SkinHelper() }

    private val vb by fvClassRv(
        listOf(
            SkinActivity::class,
            BluetoothActivity::class,
            TaskActivity::class,
            DialogActivity::class,
            ResultActivity::class,
            FileActivity::class,
            NetActivity::class,
            LogActivity::class,
            RecordActivity::class,
            WeightActivity::class,
            AboutActivity::class,
        )
    )
    override val showHome = false
    private val bar by lazy { BarHelper(this) }

    override fun onBar() {
        bar.colorStatusBar(getColorPrimary())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        skin.apply(this)
        super.onCreate(savedInstanceState)
        DataHelper.getStartUp()?.let { startActivity(Intent(this, it)) }
        vb.init()
        skin.onUpdate {
            val primary = SkinHelper.getColor(ctx, R.color.colorPrimary)
            val onPrimary = SkinHelper.getColor(ctx, R.color.colorOnPrimary)
            supportActionBar?.apply {
                setBackgroundDrawable(ColorDrawable(primary))
                title = title?.color(onPrimary)
                val home =
                    getAttrArrayFromTheme(android.R.attr.homeAsUpIndicator)
                    { getDrawable(0)?.apply { setTint(onPrimary) } }
                setHomeAsUpIndicator(home)
            }
            bar.colorStatusBar(primary)
        }
    }
}