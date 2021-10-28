package com.munch.project.one.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.app.AppHelper

/**
 * Create by munch1182 on 2021/10/14 16:36.
 */
object Permissions {

    private val context: Context
        get() = AppHelper.app
    private val uri = Uri.parse("package:${context.packageName}")

    @SuppressLint("InlinedApi", "BatteryLife")
    val PERMISSIONS = arrayListOf(
        PB(Manifest.permission.BLUETOOTH, note = "非运行时权限"),
        PB(Manifest.permission.CAMERA),
        PB(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, maxVersion = Build.VERSION_CODES.Q,
            note = "Android10及以上此权限无效"
        ),
        PB(Manifest.permission.ACCESS_FINE_LOCATION, note = "此权限有本次运行允许选项"),
        PB(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION, minVersion = Build.VERSION_CODES.Q,
            note = "此权限需要先申请定位权限；在Android10中，需要和定位权限一起申请，单独申请会直接被拒绝；在Android11中，只能获得定位权限之后，才能再进行此权限的申请"
        ),
        PB(Manifest.permission.CAMERA, note = "此权限有本次运行允许选项"),
        PB(Manifest.permission.RECORD_AUDIO, note = "此权限有本次运行允许选项"),
        PB(
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            minVersion = Build.VERSION_CODES.R,
            note = "文件管理权限",
            intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        ),
        PB(
            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            minVersion = Build.VERSION_CODES.O,
            note = "只能通过跳转页面授予权限",
            isGrantedJudge = { context.packageManager.canRequestPackageInstalls() },
            intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
        ),
        PB(
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            minVersion = Build.VERSION_CODES.M,
            note = "只能通过跳转页面授予权限",
            isGrantedJudge = { Settings.canDrawOverlays(context) },
            intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
        ),
        PB(
            Manifest.permission.WRITE_SETTINGS,
            minVersion = Build.VERSION_CODES.M,
            note = "只能通过跳转页面授予权限",
            isGrantedJudge = { Settings.System.canWrite(context) },
            intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, uri)
        ),
        PB(
            Manifest.permission.PACKAGE_USAGE_STATS,
            note = "只能通过跳转页面授予权限",
            isGrantedJudge = {
                val pm = context.packageManager
                val info = pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                val manager = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        manager?.unsafeCheckOp(
                            AppOpsManager.OPSTR_GET_USAGE_STATS,
                            info.uid,
                            info.packageName
                        ) == AppOpsManager.MODE_ALLOWED
                    } else {
                        @Suppress("Deprecation")
                        manager?.checkOp(
                            AppOpsManager.OPSTR_GET_USAGE_STATS,
                            info.uid,
                            info.packageName
                        ) == AppOpsManager.MODE_ALLOWED
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            },
            intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, uri)
        ),
        PB(
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            note = "此权限申请方法看起来是弹窗，但其实是一个四周透明的页面",
            isGrantedJudge = {
                (context.getSystemService(Context.POWER_SERVICE) as? PowerManager)
                    ?.isIgnoringBatteryOptimizations(context.packageName) ?: false
            },
            intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, uri)
        ),
        PB(
            "NOTIFICATION LISTENER", note = "此权限并不是标准权限，不需要注册",
            isGrantedJudge = {
                NotificationManagerCompat.getEnabledListenerPackages(context)
                    .contains(context.packageName)
            },
            //不能添加uri
            intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        ),
        PB(
            "NOTIFICATION", note = "此权限并不是标准权限，不需要注册",
            minVersion = Build.VERSION_CODES.O,
            isGrantedJudge = { NotificationManagerCompat.from(context).areNotificationsEnabled() },
            intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, context.applicationInfo.uid)
            }
        )
    )
}