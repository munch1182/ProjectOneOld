package com.munch.project.launcher.set

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import com.munch.pre.lib.extend.addPadding
import com.munch.pre.lib.extend.clickItem
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.helper.BarHelper
import com.munch.pre.lib.helper.IntentHelper
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.databinding.ActivitySetBinding
import com.munch.project.launcher.extend.bind
import com.munch.project.launcher.helper.BatteryOpHelper

/**
 * Create by munch1182 on 2021/5/14 11:21.
 */
class SettingActivity : BaseActivity() {

    private val bind by bind<ActivitySetBinding>(R.layout.activity_set)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        bind.setContainer.addPadding(0, AppHelper.PARAMETER.getStatusBarHeight(), 0, 0)
        bind.setContainer.clickItem({ _, pos ->
            when (pos) {
                0 -> defaultApp()
                1 -> defaultApp2()
                2 -> startDevelop()
                3 -> battery()
                4 -> whiteList()
                5 -> queryLog()
            }
        }, FrameLayout::class.java)
    }

    private fun battery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (BatteryOpHelper.isIgnoringBatteryOptimizations(this) != true) {
                startActivity(BatteryOpHelper.getRequestOptimizationsIntent(this))
            } else {
                toast("已忽略电量优化")
                startActivity(BatteryOpHelper.getRequestOptimizationsSettingIntent())
            }
        } else {
            toast("版本低于23，无法忽略电量优化")
        }
    }

    private fun whiteList() {
        BatteryOpHelper.toWhiteList(this)
    }

    private fun queryLog() {
    }

    override fun handleBar() {
        /*super.handleBar()*/
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            BarHelper(this).hideStatusBar(true).colorStatusBar(Color.TRANSPARENT)
                .setTextColorBlack()
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            super.handleBar()
        }
    }

    private fun startDevelop() {
        try {
            startActivity(IntentHelper.developmentIntent())
        } catch (e: Exception) {
            startActivity(IntentHelper.setIntent())
        }
    }

    private fun defaultApp() {
        try {
            startActivity(IntentHelper.advancedIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            toast("无法打开该页面")
        }
    }

    private fun defaultApp2() {
        startActivity(IntentHelper.appIntent())
    }
}