package com.munch.test.project.one.net

import com.munch.test.project.one.base.BaseRvActivity
import com.munch.test.project.one.net.capture.NetVpnActivity
import com.munch.test.project.one.net.clip.NetClipActivity
import com.munch.test.project.one.net.service.NetServiceActivity

/**
 * Create by munch1182 on 2021/4/14 10:55.
 */
class NetActivity : BaseRvActivity() {
    override fun getClassItem(): MutableList<ItemClassBean> {
        return ItemClassBean.newItems(
            NetIpActivity::class.java,
            NetClipActivity::class.java,
            NetServiceActivity::class.java,
            NetVpnActivity::class.java
        )
    }
}