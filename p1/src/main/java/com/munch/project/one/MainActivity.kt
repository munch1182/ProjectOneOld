package com.munch.project.one

import android.os.Bundle
import com.munch.lib.base.startActivity
import com.munch.lib.fast.base.BaseActivity
import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.fast.base.BaseRvActivity
import com.munch.lib.fast.base.toSelectActivityIfHave
import com.munch.project.one.about.AboutActivity
import com.munch.project.one.bluetooth.BluetoothActivity
import com.munch.project.one.broadcast.LogReceiveActivity
import com.munch.project.one.contentobserver.ObserverActivity
import com.munch.project.one.file.FileFunActivity
import com.munch.project.one.file.MappedByteBufferActivity
import com.munch.project.one.intent.IntentActivity
import com.munch.project.one.net.NetActivity
import com.munch.project.one.permissions.PermissionActivity
import com.munch.project.one.test.HandlerActivity
import com.munch.project.one.test.LogActivity
import com.munch.project.one.timer.AlarmActivity
import com.munch.project.one.timer.WorkManagerActivity
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
            BluetoothActivity::class.java,
            NetActivity::class.java,
            WeightActivity::class.java,
            ObserverActivity::class.java,
            TestActivity::class.java,
            AboutActivity::class.java
        )

    override fun getData() = targets.map { it.simpleName.replace("Activity", "") }.toMutableList()

    override fun canBack() = false

    override fun showMenu() {
        //禁止循环跳转
        /*super.showNotice()*/
    }
}

class TestActivity : BaseRvActivity() {
    override val targets: MutableList<Class<out BaseActivity>> =
        mutableListOf(
            HandlerActivity::class.java,
            LogActivity::class.java,
            FileActivity::class.java,
            WebActivity::class.java,
            TimerActivity::class.java,
        )

    override fun getData() = targets.map { it.simpleName.replace("Activity", "") }.toMutableList()
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

class FileActivity : BaseBtnFlowActivity() {
    override fun getData() = mutableListOf("File Fun", "MappedByteBuffer")
    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            0 -> startActivity(FileFunActivity::class.java)
            1 -> startActivity(MappedByteBufferActivity::class.java)
        }
    }
}

class TimerActivity : BaseBtnFlowActivity() {
    override fun getData() = mutableListOf("Alarm", "Work Manager")
    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            0 -> startActivity(AlarmActivity::class.java)
            1 -> startActivity(WorkManagerActivity::class.java)
        }
    }
}