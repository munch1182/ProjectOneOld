package com.munch.test.project.one

import android.os.Bundle
import com.munch.test.project.one.base.BaseRvActivity
import com.munch.test.project.one.bluetooth.BluetoothActivity
import com.munch.test.project.one.dag.DagActivity
import com.munch.test.project.one.dialog.DialogActivity
import com.munch.test.project.one.file.FileActivity
import com.munch.test.project.one.info.InfoActivity
import com.munch.test.project.one.intent.IntentActivity
import com.munch.test.project.one.log.LogActivity
import com.munch.test.project.one.recyclerview.RecyclerViewActivity
import com.munch.test.project.one.switch.SwitchActivity
import com.munch.test.project.one.thread.ThreadActivity
import com.munch.test.project.one.view.WeightActivity

class MainActivity : BaseRvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noBack()
    }

    override fun getClassItem(): MutableList<ItemClassBean> {
        return ItemClassBean.newItems(
            IntentActivity::class.java,
            DialogActivity::class.java,
            RecyclerViewActivity::class.java,
            BluetoothActivity::class.java,
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