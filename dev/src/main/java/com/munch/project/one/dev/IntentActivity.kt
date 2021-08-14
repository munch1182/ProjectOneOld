package com.munch.project.one.dev

import com.munch.lib.fast.base.BaseBtnFlowActivity

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
}