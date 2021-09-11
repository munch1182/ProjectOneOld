package com.munch.project.one.dev

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.util.*

/**
 * Create by munch1182 on 2021/9/2 13:44.
 */
object RunBackgroundHelp {
    /**
     * 根据品牌跳转后台运行设置页面
     *
     * adb shell dumpsys activity top 查看顶部activity
     * adb shell "dumpsys activity top | grep ACTIVITY | tail -n 1"
     * adb shell "dumpsys activity top | grep '#0: ' | tail -n 1"
     *
     * adb shell dumpsys usagestats >log.txt 用于查看app最后活跃时间
     */
    fun request(context: Context) {
        var brand = Build.BRAND
        brand = brand?.lowercase(Locale.getDefault()) ?: ""
        try {
            when (brand) {
                "huawei", "honnor" -> startHuawei(context, 0)
                "xiaomi" -> {
                    startActivity(
                        context, ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
                    )
                    //应该跳转小米后台配置
                    /*context.startActivity(Intent().apply {
                        component =
                            ComponentName.unflattenFromString("com.miui.powerkeeper/.ui.HiddenAppsConfigActivity")
                        putExtra("packagename", context.packageName)
                        data = Uri.fromParts("packagename", context.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })*/
                }
                "vivo" -> {
                    val intent = Intent()
                    intent.component = ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity"
                    )
                    intent.action = "secure.intent.action.softPermissionDetail"
                    intent.putExtra("packagename", context.packageName)
                    context.startActivity(intent)
                }
                "meizu" -> startActivity(
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
                "smartisan" -> context.startActivity(Intent("com.smartisanos.security"))
                /*"oneplus" -> startActivity(
                    context, ComponentName(
                        "com.oneplus.security",
                        "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                    )
                )*/
                "oppo" -> context.startActivity(
                    Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                        .setData(Uri.fromParts("package", context.packageName, null))
                )
                else -> context.startActivity(
                    Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                        .setData(Uri.fromParts("package", context.packageName, null))
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    private fun startActivity(context: Context, componentName: ComponentName?) {
        if (componentName == null) {
            throw RuntimeException()
        }
        context.startActivity(Intent().setComponent(componentName))
    }

    private fun startSamsung(context: Context, tryCount: Int) {
        val intent = Intent()
        val componentName = when (tryCount) {
            0 -> ComponentName(
                "com.samsung.android.sm_cn",
                "com.samsung.android.sm.ui.ram.AutoRunActivity"
            )
            1 ->
                ComponentName(
                    "com.samsung.android.sm_cn",
                    "com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity"
                )
            else -> throw RuntimeException()
        }
        intent.component = componentName
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            startSamsung(context, tryCount + 1)
        }
    }

    private fun startHuawei(context: Context, tryCount: Int) {
        val intent = Intent()
        val component = when (tryCount) {
            0 -> ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
            1 -> ComponentName.unflattenFromString("com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity")
            2 -> ComponentName.unflattenFromString("com.huawei.systemmanager/com.huawei.permissionmanager.ui.MainActivity")
            else -> throw RuntimeException()
        }
        intent.component = component
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            startHuawei(context, tryCount + 1)
        }
    }
}