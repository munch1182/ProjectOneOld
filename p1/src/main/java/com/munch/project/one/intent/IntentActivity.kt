package com.munch.project.one.intent

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.result.with

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
        "NotifyListener",
        "Notify",
        "Development",
        "Advance",
        "Usage",
        "WriteSettings",
        "Background"
    )

    private fun intent() = Intent().apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

    private fun intent(action: String) =
        Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

    private fun intent(action: String, uri: Uri) =
        Intent(action, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

    @SuppressLint("BatteryLife")
    override fun onClick(pos: Int) {
        super.onClick(pos)
        try {
            val onResult: (result: Boolean) -> Unit = { /*toast(if (it) "true" else "false")*/ }
            when (pos) {
                //set
                0 -> with(intent(Settings.ACTION_SETTINGS)).start(onResult)
                //app
                1 -> with(
                    intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:$packageName")
                    )
                ).start(onResult)
                //all app
                2 -> with(intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS)).start(onResult)
                //Bluetooth
                3 -> with(intent(Settings.ACTION_BLUETOOTH_SETTINGS)).start(onResult)
                //Wifi
                4 -> with(intent(Settings.ACTION_WIFI_SETTINGS)).start(onResult)
                //Data Roaming
                5 -> with(intent(Settings.ACTION_DATA_ROAMING_SETTINGS)).start(onResult)
                //GPS
                6 -> with(intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)).start(onResult)
                //Call
                7 -> with(intent(Intent.ACTION_CALL_BUTTON)).start(onResult)
                //SMS
                8 -> with(
                    intent(Intent.ACTION_SENDTO, Uri.parse("smsto:10086")).putExtra("sms_body", "")
                ).start(onResult)
                //Contact
                9 -> with(intent(Intent.ACTION_PICK).setType("vnd.android.cursor.dir/phone_v2")).start(
                    onResult
                )
                //Notify
                10 -> with(intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)).start(onResult)
                11 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    with(intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        putExtra(Settings.EXTRA_CHANNEL_ID, applicationInfo.uid)
                    }).start(onResult)
                } else {
                    toast("版本低于${Build.VERSION_CODES.O},无法使用此权限")
                }
                //Development
                12 -> with(intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)).start(onResult)
                //Advance
                13 -> with(intent().setComponent(ComponentName.unflattenFromString("com.android.settings/.Settings\$AdvancedAppsActivity")))
                    .start(onResult)
                //Usage
                14 -> with(intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)).start(onResult)
                //Write Settings
                15 -> with(intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)).start(onResult)
                16 -> RunBackgroundHelp.request(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toast("没有这个页面")
        }
    }

}