package com.munch.project.launcher

import android.os.Bundle
import com.munch.lib.android.extend.bind
import com.munch.lib.android.helper.ActivityHelper
import com.munch.lib.android.helper.BarHelper
import com.munch.project.launcher.databinding.ActivityMainBinding

/**
 * Create by munch1182 on 2022/4/3 16:35.
 */
class MainActivity : BaseActivity() {

    private val bind by bind<ActivityMainBinding>()
    private val bar by lazy { BarHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.white.setOnClickListener {
            bar.extendStatusBar().setTextColorBlack(false)
        }
        bind.black.setOnClickListener {
            bar.extendStatusBar().setTextColorBlack()
        }
        bind.full1.setOnClickListener {
            bar.fullScreen()
        }
        bind.full2.setOnClickListener {
            bar.fullScreen(false    )
        }
        bind.extendNavigation1.setOnClickListener {
        }
        bind.extendNavigation2.setOnClickListener {
        }
        ActivityHelper.getInstance()
    }
}