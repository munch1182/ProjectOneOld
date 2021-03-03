package com.munch.project.test

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.munch.lib.common.RouterHelper
import com.munch.lib.common.start2Component
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.test.bluetooth.TestBluetoothActivity
import com.munch.project.test.img.TestImgActivity
import com.munch.project.test.switch.TestSwitchActivity

/**
 * Create by munch1182 on 2020/12/7 13:58.
 */
@Route(path = RouterHelper.Test.MAIN)
open class TestMainActivity : TestRvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addEndView(getTestView("testSimple"))
        setTitle(R.string.test_app_main_title)
    }

    override fun notShowBack() = true

    override fun testFun() {
        super.testFun()
        start2Component(RouterHelper.TestSimple.MAIN)
    }

    override fun getItems(): MutableList<TestRvItemBean> {
        return mutableListOf(
            TestRvItemBean.newInstance("Img", TestImgActivity::class.java),
            TestRvItemBean.newInstance("View", TestViewActivity::class.java),
            TestRvItemBean.newInstance("Switch", TestSwitchActivity::class.java),
            TestRvItemBean.newInstance("File", TestFileMainActivity::class.java),
            TestRvItemBean.newInstance("Bluetooth", TestBluetoothActivity::class.java)
        )
    }
}