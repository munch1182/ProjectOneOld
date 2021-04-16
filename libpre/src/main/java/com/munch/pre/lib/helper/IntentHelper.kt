package com.munch.pre.lib.helper

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.munch.pre.lib.COMPATIBILITY

/**
 * Create by munch1182 on 2021/4/1 11:18.
 */
@COMPATIBILITY
object IntentHelper {

    fun setIntent() = Intent(Settings.ACTION_SETTINGS)
    fun wifiIntent() = Intent(Settings.ACTION_WIFI_SETTINGS)
    fun simIntent() = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
    fun locationIntent() = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    fun inputIntent() = Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS)
    fun localeIntent() = Intent(Settings.ACTION_LOCALE_SETTINGS)
    fun appInfoIntent(pkgName: String): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkgName"))
    }

    fun appIntent() = Intent(Settings.ACTION_APPLICATION_SETTINGS)
    fun app2Intent() = Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS)
    fun app3Intent() = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
    fun bluetoothIntent() = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
    fun notifyIntent(context: Context) =
        Intent("android.settings.APP_NOTIFICATION_SETTINGS")
            .putExtra("app_package", context.packageName)
            .putExtra("app_uid", context.applicationInfo.uid)

    fun callIntent(tel: String) = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
    fun contactsIntent() = Intent(Intent.ACTION_PICK)
        .setType("vnd.android.cursor.dir/phone_v2")

    fun smsIntent(sendTo: String?, content: String?): Intent {
        return Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$sendTo"))
            .putExtra("sms_body", content)
    }

    @RequiresPermission("android.permission.PACKAGE_USAGE_STATS")
    fun usageIntent() = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    @RequiresApi(Build.VERSION_CODES.R)
    @RequiresPermission("android.permission.MANAGE_EXTERNAL_STORAGE")
    fun allFileAccess() =
        Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun shareIntent(content: String) =
        Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, content).setType("text/plain")

    fun developmentIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
    }

    fun developmentIntent2(): Intent {
        return Intent().setComponent(
            ComponentName(
                "com.android.settings",
                "com.android.settings.DevelopmentSettings"
            )
        ).setAction("android.intent.action.View")
    }

    @Throws(StartActivityFailException::class)
    fun startDevelopment(context: Context) {
        startDevelopment(context, 0)
    }

    private fun startDevelopment(context: Context, tryCount: Int) {
        try {
            when (tryCount) {
                0 -> context.startActivity(developmentIntent())
                1 -> context.startActivity(developmentIntent2())
                else -> throw StartActivityFailException("develop")
            }
        } catch (e: ActivityNotFoundException) {
            startDevelopment(context, tryCount + 1)
        }
    }

    class StartActivityFailException(s: String) : Exception("cannot start activity to $s page.")
}