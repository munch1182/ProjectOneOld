package com.munch.lib.helper

import android.content.Context
import android.content.pm.PackageManager
import com.munch.lib.BaseApp

/**
 * Create by munch1182 on 2020/12/28 17:50.
 */
object AppHelper {

    @Suppress("DEPRECATION")
    fun getVersionCode(context: Context = BaseApp.getInstance()): Long {
        val packageInfo = context.packageManager?.getPackageInfo(
            context.packageName,
            PackageManager.GET_CONFIGURATIONS
        ) ?: return 0L
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
    }
}