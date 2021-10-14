package com.munch.project.one.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.munch.lib.app.AppHelper

/**
 * Create by munch1182 on 2021/10/14 16:36.
 */
object Permissions {

    @SuppressLint("InlinedApi")
    val PERMISSIONS = arrayListOf(
        PB(Manifest.permission.CALL_PHONE),
        PB(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, maxVersion = Build.VERSION_CODES.Q,
            note = "Android10及以上此权限无效"
        ),
        PB(Manifest.permission.ACCESS_FINE_LOCATION, note = "此权限有本次运行允许选项"),
        PB(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION, minVersion = Build.VERSION_CODES.Q,
            note = "此权限需要先申请定位权限；在Android10中，需要和定位权限一起申请，单独申请会直接被拒绝；在Android11中，只能获得定位权限之后，才能再进行此权限的申请"
        ),
        PB(
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            minVersion = Build.VERSION_CODES.R,
            note = "文件管理权限"
        ),
        PB(Manifest.permission.CAMERA, note = "此权限有本次运行允许选项"),
        PB(Manifest.permission.RECORD_AUDIO, note = "此权限有本次运行允许选项"),
        PB(
            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            minVersion = Build.VERSION_CODES.O,
            note = "只能通过跳转页面授予权限",
            isGrantedJudge = { AppHelper.app.packageManager.canRequestPackageInstalls() },
            intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${AppHelper.app.packageName}")
            )
        ),
        PB(
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            minVersion = Build.VERSION_CODES.M,
            note = "只能通过跳转页面授予权限",
            isGrantedJudge = { Settings.canDrawOverlays(AppHelper.app) },
            intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${AppHelper.app.packageName}")
            )
        ),
        PB(
            Manifest.permission.WRITE_SETTINGS,
            minVersion = Build.VERSION_CODES.M,
            note = "只能通过跳转页面授予权限",
            isGrantedJudge = { Settings.System.canWrite(AppHelper.app) },
            intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:${AppHelper.app.packageName}")
            )
        )
    )
}