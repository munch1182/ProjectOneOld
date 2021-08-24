package com.munch.project.one.dev

import android.Manifest
import android.content.Intent
import android.provider.Settings
import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.log.log
import com.munch.lib.result.ResultHelper

/**
 * Create by munch1182 on 2021/8/10 15:41.
 */
class IntentActivity : BaseBtnFlowActivity() {

    override fun getData() =
        mutableListOf(
            "Set", "App", "All App",
            "Bluetooth", "Wifi", "Location", "Call", "SMS", "Contact",
            "Notify", "Development", "Usage", "File(30)", "Battery Optimize"
        )

    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            0 -> ResultHelper.init(this)
                .with(Intent(Settings.ACTION_SETTINGS))
                .start(object : ResultHelper.OnResultListener {
                    override fun onResult(isOk: Boolean, resultCode: Int, data: Intent?) {
                        log(isOk, resultCode, data)
                    }
                })
            1 -> ResultHelper.init(this)
                .with(Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE)
                .request(object : ResultHelper.OnPermissionResultListener {
                    override fun onResult(
                        allGrant: Boolean,
                        grantedList: ArrayList<String>,
                        deniedList: ArrayList<String>
                    ) {
                    }
                })
        }
    }
}