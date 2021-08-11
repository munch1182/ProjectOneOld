package com.munch.project.one.dev

import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.weight.FlowLayout

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
            0 -> flowLayout.setGravity(FlowLayout.START)
            1 -> flowLayout.setGravity(FlowLayout.END)
            2 -> flowLayout.setGravity(FlowLayout.CENTER)
            3 -> flowLayout.setGravity(FlowLayout.CENTER_HORIZONTAL)
            4 -> flowLayout.setGravity(FlowLayout.CENTER_VERTICAL)
            5 -> flowLayout.setGravity(FlowLayout.END_CENTER_VERTICAL)
        }
    }
}