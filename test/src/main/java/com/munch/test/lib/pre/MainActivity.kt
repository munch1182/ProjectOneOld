package com.munch.test.lib.pre

import android.os.Bundle
import com.munch.lib.fast.base.activity.BaseRvActivity
import com.munch.pre.lib.extend.startActivity
import com.munch.test.lib.pre.dag.DagActivity
import com.munch.test.lib.pre.info.InfoActivity
import com.munch.test.lib.pre.intent.IntentActivity
import com.munch.test.lib.pre.log.LogActivity
import com.munch.test.lib.pre.thread.ThreadActivity

class MainActivity : BaseRvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noBack()
        startActivity(DagActivity::class.java)
    }

    override fun getClassItem(): MutableList<ItemClassBean> {
        return ItemClassBean.newItems(
            IntentActivity::class.java,
            ThreadActivity::class.java,
            DagActivity::class.java,
            TestActivity::class.java,
            Test2Activity::class.java,
            LogActivity::class.java,
            InfoActivity::class.java
        )
    }
}