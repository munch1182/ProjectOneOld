package com.munch.test.lib.pre

import android.os.Bundle
import com.munch.test.lib.pre.base.BaseRvActivity
import com.munch.test.lib.pre.dag.DagActivity
import com.munch.test.lib.pre.dialog.DialogActivity
import com.munch.test.lib.pre.file.FileActivity
import com.munch.test.lib.pre.info.InfoActivity
import com.munch.test.lib.pre.intent.IntentActivity
import com.munch.test.lib.pre.log.LogActivity
import com.munch.test.lib.pre.switch.SwitchActivity
import com.munch.test.lib.pre.thread.ThreadActivity
import com.munch.test.lib.pre.view.WeightActivity

class MainActivity : BaseRvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noBack()
    }

    override fun getClassItem(): MutableList<ItemClassBean> {
        return ItemClassBean.newItems(
            IntentActivity::class.java,
            DialogActivity::class.java,
            ThreadActivity::class.java,
            FileActivity::class.java,
            DagActivity::class.java,
            SwitchActivity::class.java,
            WeightActivity::class.java,
            TestActivity::class.java,
            Test2Activity::class.java,
            LogActivity::class.java,
            InfoActivity::class.java
        )
    }
}