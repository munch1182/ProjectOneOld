package com.munch.project.one.dev

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
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
        "Usage",
        "File(30)",
        "Battery Optimize",
        "Write Setting",
        "Background"
    )

    @SuppressLint("BatteryLife")
    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            //set
            0 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_SETTINGS))
                .start { }
            //app
            1 -> ResultHelper.init(this)
                .with(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:$packageName")
                    )
                )
                .start { }
            //all app
            2 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS))
                .start { }
            //Bluetooth
            3 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                .start { }
            //Wifi
            4 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_WIFI_SETTINGS))
                .start { }
            //Data Roaming
            5 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
                .start { }
            //Location
            6 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                .start { }
            //Call
            7 -> ResultHelper.init(this)
                .with(Intent(Intent.ACTION_DIAL, Uri.parse("tel:10086")))
                .start { }
            //SMS
            8 -> ResultHelper.init(this)
                .with(
                    Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:10086"))
                        .putExtra("sms_body", "1")
                )
                .start { }
            //Contact
            9 -> ResultHelper.init(this)
                .with(
                    Intent(Intent.ACTION_PICK).setType("vnd.android.cursor.dir/phone_v2")
                )
                .start { }
            //Notify
            10 -> ResultHelper.init(this)
                .with(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                .start { }
            //Development
            11 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                .start { }
            //Usage,android.permission.PACKAGE_USAGE_STATS
            12 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                .start { }
            //File(30),android.permission.MANAGE_EXTERNAL_STORAGE
            13 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ResultHelper.init(this)
                    .with(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                    .start { }
            } else {
                toast("版本低于30")
            }
            //Battery Optimize
            14 -> ResultHelper.init(this)
                .with(Intent(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)))
                .start { }
            // WRITE SETTING
            15 -> ResultHelper.init(this)
                .with(
                    Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:$packageName")
                    )
                )
                .start { }
            //Background
            16 -> RunBackgroundHelp.request(this)
        }
    }
}