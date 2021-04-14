package com.munch.test.project.one.intent

import com.munch.pre.lib.helper.IntentHelper
import com.munch.test.project.one.base.BaseItemActivity

/**
 * Create by munch1182 on 2021/4/1 11:16.
 */
class IntentActivity : BaseItemActivity() {

    override fun clickItem(pos: Int) {
        when (pos) {
            0 -> startActivity(IntentHelper.setIntent())
            1 -> startActivity(IntentHelper.appInfoIntent(this.packageName))
            2 -> startActivity(IntentHelper.appIntent())
            3 -> startActivity(IntentHelper.locationIntent())
            4 -> startActivity(IntentHelper.callIntent("00000000000"))
            5 -> startActivity(IntentHelper.smsIntent("10086", "10001"))
            6 -> startActivity(IntentHelper.contactsIntent())
            7 -> startActivity(IntentHelper.wifiIntent())
            8 -> startActivity(IntentHelper.bluetoothIntent())
            9 -> startActivity(IntentHelper.notifyIntent(this))
            10 -> IntentHelper.startDevelopment(this)
            11 -> startActivity(IntentHelper.shareIntent("share content temp"))
            12 -> startActivity(IntentHelper.usageIntent())
        }
    }

    override fun getItem(): MutableList<String> {
        return mutableListOf(
            "set",
            "app",
            "all app",
            "location",
            "call",
            "message",
            "contact",
            "wifi",
            "bluetooth",
            "notify",
            "development",
            "share",
            "usage"
        )
    }
}