package com.munch.project.testsimple

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import com.munch.lib.helper.SpHelper
import com.munch.lib.helper.isServiceRunning
import com.munch.lib.test.TestBaseTopActivity
import com.munch.lib.test.TestDialog
import com.munch.project.testsimple.alive.TestDataHelper
import com.munch.project.testsimple.alive.battery.BatteryOpHelper
import com.munch.project.testsimple.alive.mix.WorkService
import com.munch.project.testsimple.alive.work.RestartWork

/**
 * 专门为测试alive-mix
 * Create by munch1182 on 2020/12/18 10:19.
 */
class TestAliveSimpleActivity : TestBaseTopActivity() {

    private val phone by lazy { findViewById<TextView>(R.id.test_alive_phone) }
    private val info by lazy { findViewById<TextView>(R.id.test_alive_info) }
    private val btnTest by lazy { findViewById<TextView>(R.id.test_alive_btn) }
    private val btnStart by lazy { findViewById<TextView>(R.id.test_alive_btn_auto_start) }
    private val see by lazy { findViewById<TextView>(R.id.test_alive_btn_see) }


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_alive)

        showBack(false)
        title = "测试存活时间"

        phone.text = "${Build.BRAND},android:${Build.VERSION.SDK_INT}"
        btnTest.setOnClickListener {
            clearOld()
            TestDialog.SimpleDialog(this)
                .setContent("需要开启后台并忽略电量优化。如果是智能后台或者自动管理的，需要关闭智能后台或者自动管理，并手动允许后台运行或者启动。")
                .setConfirmListener {
                    mixAlive()
                    SpHelper.getSp().put("start_test", true)
                }
                .show()
        }
        btnStart.setOnClickListener {
            BatteryOpHelper.toAutoStart(this)
        }
        see.setOnClickListener {
            showInfo()
        }
    }

    private fun clearOld() {
        /*stopAllService()*/
        RestartWork.stop(this)
        SpHelper.getSp().put("start_test", false)
        TestDataHelper.clear()
        info.text = ""
        /*info.text = "已关闭所有后台服务"*/
    }

    private fun mixAlive() {
        //电量优化-》回调自启动
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (BatteryOpHelper.isIgnoringBatteryOptimizations(this) != true) {
                startActivityForResult(
                    BatteryOpHelper.getRequestOptimizationsIntent(this),
                    1216
                )
            }
        }
        //work
        RestartWork.work(this)
        WorkService.start(this)
        info.postDelayed({
            showInfo()
        }, 1000)
    }

    private fun showInfo() {
        if (!SpHelper.getSp().get("start_test", false)!!) {
            this.info.text = "测试未开始"
            return
        }
        var info = ""
        val serviceRunning = isServiceRunning(WorkService::class.java)
        if (serviceRunning == true) {
            info += "后台服务正在运行，如果未看见通知，请打开通知权限,\r\n"
            info += TestDataHelper.getMixTestData(this) + ",\r\n"
        } else if (serviceRunning == false) {
            info += "后台服务已被关闭,\r\n"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (BatteryOpHelper.isIgnoringBatteryOptimizations(this) == true) {
                info += "电量优化已关闭"
            }
        }

        this.info.text = info
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        BatteryOpHelper.toAutoStart(this)
    }
}