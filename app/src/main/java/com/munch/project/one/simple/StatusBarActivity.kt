package com.munch.project.one.simple

import android.graphics.Color
import android.os.Bundle
import com.munch.lib.android.extend.bind
import com.munch.lib.android.extend.lazy
import com.munch.lib.android.extend.newRandomColor
import com.munch.lib.android.helper.BarHelper
import com.munch.lib.fast.view.dialog.DialogHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.launch
import com.munch.plugin.annotation.Measure
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.ActivityStatusBarBinding
import com.munch.project.one.net.BiYing
import com.munch.project.one.other.ImageHelper.load
import kotlinx.coroutines.Dispatchers
import kotlin.random.Random

/**
 * Create by munch1182 on 2022/9/22 16:36.
 */
@Measure
class StatusBarActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private val bind by bind<ActivityStatusBarBinding>()
    private val bar by lazy { BarHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(bind.barTb)
        bar.extendStatusBar().colorStatusBar()
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true) }
        bind.barTb.navigationIcon?.setTint(Color.WHITE)
        bind.apply {
            barExtend.setOnClickListener { extendBar() }
            barColor.setOnClickListener { colorBar() }
            barDialog.setOnClickListener { dialog() }
            navigationColor.setOnClickListener { extendNavigation() }
        }

        launch(Dispatchers.IO) {
            val f = if (BiYing.curr.exists()) {
                BiYing.curr
            } else {
                val files = BiYing.curr.parentFile?.listFiles()
                val index = files?.size?.let { Random.nextInt(it) } ?: 0
                files?.get(index)
            }
            bind.barImage.load(f)
        }
    }

    private fun extendNavigation() {
        bar.colorNavigation(newRandomColor())
    }

    private fun dialog() {
        DialogHelper.bottom()
            .title("BottomDialog")
            .content("dialog from bottom")
            .cancel()
            .show()
    }

    private fun colorBar() {
        bar.colorStatusBar(newRandomColor(0.5f))
    }

    private fun extendBar() {
        bar.extendStatusBar(!bar.isExtendStatusBar)
    }

}