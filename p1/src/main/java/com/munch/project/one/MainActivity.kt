package com.munch.project.one

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.munch.lib.base.startActivity
import com.munch.lib.fast.base.BaseActivity
import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.fast.base.BaseRvActivity
import com.munch.lib.fast.base.toSelectActivityIfHave
import com.munch.lib.log.log
import com.munch.project.one.about.AboutActivity
import com.munch.project.one.broadcast.LogReceiveActivity
import com.munch.project.one.contentobserver.ObserverActivity
import com.munch.project.one.file.FileExploreActivity
import com.munch.project.one.intent.IntentActivity
import com.munch.project.one.net.NetActivity
import com.munch.project.one.permissions.PermissionActivity
import com.munch.project.one.test.*
import com.munch.project.one.timer.AlarmActivity
import com.munch.project.one.timer.WorkManagerActivity
import com.munch.project.one.web.WebActivity
import com.munch.project.one.weight.FlowLayoutActivity
import com.munch.project.one.weight.LoadingActivity
import com.munch.project.one.weight.WheelViewActivity

class MainActivity : BaseRvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cost = App.getLaunchCost()
        if (cost > 500L) {
            log("冷启动用时:$cost ms")
        }
        //冷启动
        if (cost > -1) {
            container.post { toSelectActivityIfHave() }
        } else {
            toSelectActivityIfHave()
        }
    }

    override fun setContentView(layoutResID: Int) {
        installSplashScreen()
        //因为无法判断绘制之后的时间点，因此如果要延长splash时间，直接暂停主线程即可
        /*Thread.sleep(max(0L, 800L - cost))*/
        super.setContentView(layoutResID)
    }

    override val targets: MutableList<Class<out BaseActivity>> =
        mutableListOf(
            IntentActivity::class.java,
            PermissionActivity::class.java,
            LogReceiveActivity::class.java,
            NetActivity::class.java,
            FileExploreActivity::class.java,
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
            FileFunActivity::class.java,
            MappedByteBufferActivity::class.java,
            WebActivity::class.java,
            TimerActivity::class.java,
            NotificationActivity::class.java,
            ExceptionActivity::class.java
        )

    override fun getData() = targets.map { it.simpleName.replace("Activity", "") }.toMutableList()
}

class WeightActivity : BaseBtnFlowActivity() {

    override fun getData() = mutableListOf("FlowLayout", "Loading", "WheelView")

    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            0 -> startActivity(FlowLayoutActivity::class.java)
            1 -> startActivity(LoadingActivity::class.java)
            2 -> startActivity(WheelViewActivity::class.java)
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