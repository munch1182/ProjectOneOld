package com.munch.project.one

import android.os.Bundle
import com.munch.lib.base.startActivity
import com.munch.lib.fast.base.BaseActivity
import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.fast.base.BaseRvActivity
import com.munch.lib.fast.base.toSelectActivityIfHave
import com.munch.lib.fast.log.LogActivity
import com.munch.project.one.about.AboutActivity
import com.munch.project.one.bluetooth.BluetoothActivity
import com.munch.project.one.broadcast.LogReceiveActivity
import com.munch.project.one.file.FileActivity
import com.munch.project.one.handler.HandlerActivity
import com.munch.project.one.intent.IntentActivity
import com.munch.project.one.net.NetActivity
import com.munch.project.one.permissions.PermissionActivity
import com.munch.project.one.web.WebActivity
import com.munch.project.one.weight.FlowLayoutActivity

class MainActivity : BaseRvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toSelectActivityIfHave()
    }

    override val targets: MutableList<Class<out BaseActivity>> =
        mutableListOf(
            IntentActivity::class.java,
            PermissionActivity::class.java,
            LogReceiveActivity::class.java,
            HandlerActivity::class.java,
            BluetoothActivity::class.java,
            NetActivity::class.java,
            WebActivity::class.java,
            FileActivity::class.java,
            WeightActivity::class.java,
            LogActivity::class.java,
            AboutActivity::class.java
        )

    override fun getData() = targets.map { it.simpleName.replace("Activity", "") }.toMutableList()

    override fun canBack() = false

    override fun showMenu() {
        //禁止循环跳转
        /*super.showNotice()*/

    }
}

class WeightActivity : BaseBtnFlowActivity() {

    override fun getData() = mutableListOf("FlowLayout")

    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            0 -> startActivity(FlowLayoutActivity::class.java)
        }
    }
}