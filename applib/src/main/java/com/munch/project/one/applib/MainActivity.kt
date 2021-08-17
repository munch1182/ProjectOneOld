package com.munch.project.one.applib

import android.os.Bundle
import com.munch.lib.base.startActivity
import com.munch.lib.fast.base.BaseActivity
import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.fast.base.BaseRvActivity
import com.munch.lib.fast.base.toSelectActivityIfHave
import com.munch.project.one.applib.file.TestFileActivity
import com.munch.project.one.applib.weight.TestFlowLayoutActivity

class MainActivity : BaseRvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toSelectActivityIfHave()
    }

    override val targets: MutableList<Class<out BaseActivity>> =
        mutableListOf(TestFileActivity::class.java, TestWeightActivity::class.java)

    override fun canBack() = false

    override fun showMenu() {
        //禁止循环跳转
        /*super.showNotice()*/
    }
}

class TestWeightActivity : BaseBtnFlowActivity() {

    override fun getData() = mutableListOf("FlowLayout")

    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            0 -> startActivity(TestFlowLayoutActivity::class.java)
        }
    }
}