package com.munch.project.testsimple.alive.battery

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import java.util.*

/**
 * Create by munch1182 on 2020/12/11 11:43.
 */
object BatteryOpHelper {

    @RequiresApi(Build.VERSION_CODES.M)
    fun isIgnoringBatteryOptimizations(context: Context): Boolean? {
        val manager = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
        return manager?.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * 低电量模式
     * https://developer.android.google.cn/training/monitoring-device-state/doze-standby?hl=zh_cn
     */
    @SuppressLint("BatteryLife")
    @RequiresApi(Build.VERSION_CODES.M)
    @RequiresPermission(android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
    fun getRequestOptimizationsIntent(context: Context): Intent {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:" + context.packageName)
        return intent
    }

    /**
     * 申请后台运行权限
     *
     * adb shell dumpsys activity top 查看顶部activity
     *
     * adb shell dumpsys usagestats >log.txt 用于查看app最后活跃时间
     */
    fun toAutoStart(context: Context) {
        val brand = Build.BRAND?.toLowerCase(Locale.getDefault()) ?: ""
        try {
            when (brand) {
                "huawei", "honnor" -> //华为emui10.0.0已测试
                    startHuawei(context, 0)
                "xiaomi" -> startActivity(
                    context, ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                )
                "oppo" -> //cocloros 5.2.1测试
                    context.startActivity(
                        Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                "vivo" -> context.startActivity(Intent().apply {
                    component = ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity"
                    )
                    action = "secure.intent.action.softPermissionDetail"
                    putExtra("packagename", context.packageName)
                })
                "meizu" ->//flyme8.20已测试
                    startActivity(
                        context,
                        ComponentName.unflattenFromString("com.meizu.safe/.permission.SmartBGActivity")
                    )
                "samsung" -> startSamsung(context, 0)
                "letv" -> startActivity(
                    context, ComponentName(
                        "com.letv.android.letvsafe",
                        "com.letv.android.letvsafe.AutobootManageActivity"
                    )
                )
                "oneplus" -> startActivity(
                    context, ComponentName(
                        "com.oneplus.security",
                        "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                    )
                )
                else -> context.startActivity(
                    Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(
                        Uri.fromParts("package", context.packageName, null)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    private fun startSamsung(context: Context, index: Int) {
        val intent = Intent()
        intent.component = when (index) {
            //有些版本三星没有exported这个页面，所以会报错
            0 -> ComponentName(
                "com.samsung.android.sm_cn",
                "com.samsung.android.sm.ui.ram.AutoRunActivity"
            )
            //samsung 9.5测试
            //直接跳转三星的管理器界面
            1 -> ComponentName(
                "com.samsung.android.sm_cn",
                "com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity"
            )
            else -> {
                throw Exception()
            }
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            startSamsung(context, index + 1)
        }
    }

    private fun startHuawei(context: Context, index: Int) {
        val intent = Intent()
        intent.component = when (index) {
            //emui10.0.0 已测试
            0 -> ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
            1 -> ComponentName.unflattenFromString("com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity")
            2 -> ComponentName.unflattenFromString("com.huawei.systemmanager/com.huawei.permissionmanager.ui.MainActivity")
            else -> {
                throw Exception()
            }
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            startHuawei(
                context,
                index + 1
            )
        }
    }

    private fun startActivity(context: Context, componentName: ComponentName?) {
        if (componentName == null) {
            throw Exception()
        }
        context.startActivity(Intent().setComponent(componentName))
    }
}