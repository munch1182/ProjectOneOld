package com.munch.test.lib.pre

import android.os.Bundle
import com.munch.lib.fast.base.BaseRvActivity
import com.munch.test.lib.pre.info.InfoActivity
import com.munch.test.lib.pre.intent.IntentActivity
import com.munch.test.lib.pre.log.LogActivity

class MainActivity : BaseRvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noBack()
    }

    override fun getClassItem(): MutableList<ItemClassBean> {
        return ItemClassBean.newItems(
            IntentActivity::class.java,
            LogActivity::class.java,
            InfoActivity::class.java
        )
    }
}