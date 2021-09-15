package com.munch.project.one.intent

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.result.ResultHelper

/**
 * Create by munch1182 on 2021/8/10 15:41.
 */
class IntentActivity : BaseBtnFlowActivity() {

    override fun getData() = mutableListOf(
        "Set",
        "App",
        "All App",
        "Bluetooth",
        "Wifi",
        "Data Roaming",
        "Location",
        "Call",
        "SMS",
        "Contact",
        "Notify",
        "Development",
        "Advance",
        "Usage",
        "File(30)",
        "Battery Optimize",
        "Write Setting",
        "Background"
    )

    @SuppressLint("BatteryLife")
    override fun onClick(pos: Int) {
        super.onClick(pos)
        val onResult: (result: Boolean) -> Unit = { toast(if (it) "true" else "false") }
        when (pos) {
            //set
            0 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_SETTINGS))
                .start {}
            //app
            1 -> ResultHelper.init(this)
                .with(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:$packageName")
                    )
                )
                .start {}
            //all app
            2 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS))
                .start {}
            //Bluetooth,android.permission.BLUETOOTH
            3 -> ResultHelper.init(this)
                .with(
                    { BluetoothAdapter.getDefaultAdapter().isEnabled },
                    Intent("android.bluetooth.adapter.action.REQUEST_ENABLE")
                )
                .start(onResult)
            //Wifi
            4 -> {
                ResultHelper.init(this)
                    .with(Intent(Settings.ACTION_WIFI_SETTINGS))
                    .start {}
            }
            //Data Roaming
            5 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
                .start {}
            //Location+GPS, android.permission.ACCESS_FINE_LOCATION
            //    android.permission.ACCESS_COARSE_LOCATION
            6 -> ResultHelper.init(this)
                .contactWith(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .contactWith({
                    (getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
                        ?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
                }, Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                .start(onResult)
            //Call
            7 -> ResultHelper.init(this)
                .with(Intent(Intent.ACTION_DIAL, Uri.parse("tel:10086")))
                .start {}
            //SMS
            8 -> ResultHelper.init(this)
                .with(
                    Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:10086"))
                        .putExtra("sms_body", "1")
                )
                .start {}
            //Contact
            9 -> ResultHelper.init(this)
                .with(
                    Intent(Intent.ACTION_PICK).setType("vnd.android.cursor.dir/phone_v2")
                )
                .start(onResult)
            //Notify
            10 -> ResultHelper.init(this)
                .with(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                .start(onResult)
            //Development
            11 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                .start {}
            //advance
            12 -> ResultHelper.init(this)
                .with(Intent().setComponent(ComponentName.unflattenFromString("com.android.settings/.Settings\$AdvancedAppsActivity")))
                .start {}
            //Usage,android.permission.PACKAGE_USAGE_STATS
            13 -> {
                val judge = {
                    val pm = packageManager
                    val info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                    val manager = getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
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
                }
                ResultHelper.init(this)
                    .with(judge, Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    .start(onResult)
            }
            //File(30),android.permission.MANAGE_EXTERNAL_STORAGE
            14 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ResultHelper.init(this)
                    .with(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                    .start(onResult)
            } else {
                toast("版本低于30")
            }
            //Battery Optimize,android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            // https://developer.android.google.cn/training/monitoring-device-state/doze-standby?hl=zh_cn
            15 -> {
                val judge = {
                    (getSystemService(Context.POWER_SERVICE) as? PowerManager)
                        ?.isIgnoringBatteryOptimizations(packageName) ?: false
                }
                val requestIntent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                )
                ResultHelper.init(this)
                    .with(judge, requestIntent)
                    .start(onResult)
            }
            // WRITE SETTING
            16 -> ResultHelper.init(this)
                .with(
                    Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:$packageName")
                    )
                )
                .start(onResult)
            //Background
            17 -> RunBackgroundHelp.request(this)
        }
    }

}