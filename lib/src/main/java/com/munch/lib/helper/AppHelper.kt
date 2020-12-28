package com.munch.lib.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.munch.lib.BaseApp

/**
 * Create by munch1182 on 2020/12/28 17:50.
 */
object AppHelper {

    @Suppress("DEPRECATION")
    fun getVersionCodeAndName(context: Context = getBaseApp()): Pair<Long, String>? {
        val packageInfo = context.packageManager?.getPackageInfo(
            context.packageName,
            PackageManager.GET_CONFIGURATIONS
        ) ?: return null
        return Pair(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            },
            packageInfo.versionName
        )
    }

    /**
     * 在android11，需要[android.Manifest.permission.QUERY_ALL_PACKAGES]权限
     *
     *@see [https://developer.android.google.cn/about/versions/11/privacy/package-visibility?authuser=0&hl=tr]
     *@see [https://developer.android.google.cn/training/basics/intents/package-visibility?authuser=0]
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun getInstallApp(context: Context = getBaseApp()): MutableList<PackageInfo>? {
        return context.packageManager?.getInstalledPackages(PackageManager.GET_CONFIGURATIONS)
    }

    /**
     * 通过查找app信息是否成功来判断app是否安装，以避开某些手机品牌的权限
     */
    fun isAppInstall(context: Context = getBaseApp(), pkgName: String): Boolean {
        return try {
            context.packageManager?.getPackageInfo(pkgName, PackageManager.GET_GIDS)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isSystemApp(info: PackageInfo): Boolean {
        return (info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1)
                || (info.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 1)
    }

    fun getAppIcon(context: Context = BaseApp.getInstance(), pkgName: String): Drawable? {
        try {
            return context.packageManager?.getApplicationIcon(pkgName) ?: return null
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

    private fun getBaseApp(): Context = BaseApp.getInstance()
}