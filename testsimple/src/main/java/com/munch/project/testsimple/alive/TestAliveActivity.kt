package com.munch.project.testsimple.alive

import android.content.Intent
import android.os.Build
import android.view.View
import androidx.work.WorkInfo
import com.munch.lib.helper.formatDate
import com.munch.lib.helper.startActivity
import com.munch.lib.helper.stopAllService
import com.munch.lib.log
import com.munch.lib.test.TestDialog
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.testsimple.alive.battery.BatteryOpHelper
import com.munch.project.testsimple.alive.foreground.ForegroundService
import com.munch.project.testsimple.alive.guard.KeepService
import com.munch.project.testsimple.alive.mix.WorkService
import com.munch.project.testsimple.alive.music.SilentMusicService
import com.munch.project.testsimple.alive.px.SinglePixelActivity
import com.munch.project.testsimple.alive.work.RestartWork
import java.util.*

/**
 * Create by munch1182 on 2020/12/14 10:30.
 */
class TestAliveActivity : TestRvActivity() {

    override fun setupIntent() {
        super.setupIntent()
        intent.putExtras(
            newBundle(
                "Alive",
                null,
                hasBack = true,
                isBtn = true
            )
        )
    }

    override fun onResume() {
        super.onResume()
        notifyChange()
    }

    private fun notifyChange() {
        val lastTimeForegroundAliveTime =
            TestDataHelper.getLastTimeForegroundAliveTime(
                this
            )
        notifyItem(3, "已存活$lastTimeForegroundAliveTime")

        val count =
            TestDataHelper.getGuardCount(this)
        if (count != null) {
            notifyItem(2, "已拉起${count}次")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val ignoringBatteryOptimizations = BatteryOpHelper.isIgnoringBatteryOptimizations(this)
            if (ignoringBatteryOptimizations == true) {
                notifyItem(7, "已关闭电量优化")
            }
        } else {
            notifyItem(7, "api版本低于23无效")
        }

        RestartWork.getWork(this)
            .observe(this, {
                var workCount = 0
                it.forEach { work ->
                    log(work)
                    if (work.state != WorkInfo.State.CANCELLED) {
                        workCount++
                    }
                }
                if (workCount > 0) {
                    val lastWorkTime =
                        TestDataHelper.getLastWorkTime(
                            this
                        ) ?: System.currentTimeMillis()
                    val formatDate =
                        "HH:mm:ss".formatDate(Date(System.currentTimeMillis() - lastWorkTime))
                    notifyItem(5, "有${workCount}个work，上次运行在${formatDate}之前")
                }
            })
    }

    override fun clickItem(view: View, pos: Int) {
        super.clickItem(view, pos)
        when (pos) {
            0 -> {
                TestDialog.SimpleDialog(this)
                    .setContent("需要开启后台并忽略电量优化。如果是智能后台或者自动管理的，需要关闭智能后台或者自动管理，并手动允许后台运行或者启动。")
                    .setConfirmListener {
                        mixAlive(view)
                    }
                    .show()
            }
            1 -> {
                SinglePixelActivity.register(this)
                notifyItem(pos, "已注册单像素，关闭应用即取消注册")
            }
            2 -> {
                KeepService.start(this)
                notifyItem(pos, "已开始守护进程")
            }
            3 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ForegroundService.start(this)
                }
                notifyItem(pos, "已开启前台服务")
            }
            4 -> {
                SilentMusicService.register(this)
                notifyItem(pos, "后台音乐已注册，关闭应用即取消注册")
            }
            5 -> {
                RestartWork.work(this)
            }
            6 -> {
                BatteryOpHelper.toWhiteList(this)
            }
            7 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (BatteryOpHelper.isIgnoringBatteryOptimizations(this) != true) {
                        startActivity(BatteryOpHelper.getRequestOptimizationsIntent(this))
                    } else {
                        notifyItem(pos, "已关闭电量优化")
                    }
                } else {
                    notifyItem(pos, "api版本低于23无效")
                }
            }
            8 -> {
            }
            9 -> clear()
            10 -> {
                startActivity(TestAliveSimpleActivity::class.java)
            }
            else -> {
            }
        }
    }

    /**
     * 综合使用
     * 综合了电量优化、自启动、work、前台进程和双进程守护
     */
    private fun mixAlive(view: View) {
        //电量优化-》回调自启动
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (BatteryOpHelper.isIgnoringBatteryOptimizations(this) != true) {
                startActivityForResult(
                    BatteryOpHelper.getRequestOptimizationsIntent(this),
                    1216
                )
            } else {
                notifyItem(7, "已关闭电量优化")
            }
        } else {
            notifyItem(7, "api版本低于23无效")
        }
        //work
        clickItem(view, 5)
        WorkService.start(this)
        notifyItem(0, "已开启前台服务和守护进程")
    }

    private fun clear() {
        adapter.clearItemInfo()
        TestDataHelper.clear(this)
        RestartWork.stop(this)
        stopAllService()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        clickItem(View(this), 6)
    }

    override fun getItems(): MutableList<TestRvItemBean> {
        return TestRvItemBean.newArray(
            "Mix",
            "Px Activity",
            "Guard Service",
            "Foreground Service",
            "Silent Music",
            "Work",
            "Running In Background",
            "Battery Optimizations",
            "Process",
            "Clear",
            "TestAliveSimple"
        )
    }

    private fun notifyItem(pos: Int, string: String) {
        adapter.notifyItem(pos, string)
    }

    override fun onDestroy() {
        super.onDestroy()
        log("TestAliveActivity onDestroy")
    }

}