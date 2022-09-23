package com.munch.project.one.simple

import android.graphics.Color
import android.os.Bundle
import com.munch.lib.android.extend.bind
import com.munch.lib.android.extend.lazy
import com.munch.lib.android.extend.newRandomColor
import com.munch.lib.android.extend.thread2UI
import com.munch.lib.android.helper.BarHelper
import com.munch.lib.fast.view.dialog.DialogHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.plugin.annotation.Measure
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.ActivityStatusBarBinding
import com.munch.project.one.other.load

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

        thread2UI {
            bind.barImage.load("https://cn.bing.com/th?id=OHR.SpringPoint_ZH-CN6445792697_1080x1920.jpg&rf=LaDigue_1920x1080.jpg&pid=hp")
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