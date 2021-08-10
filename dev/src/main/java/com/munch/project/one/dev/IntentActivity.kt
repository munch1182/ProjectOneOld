package com.munch.project.one.dev

import com.munch.lib.fast.base.BaseBtnWithNoticeActivity

/**
 * Create by munch1182 on 2021/8/10 15:41.
 */
class IntentActivity : BaseBtnWithNoticeActivity() {

    override fun getData() = mutableListOf("Set", "Bluetooth", "Wifi", "Location")
}