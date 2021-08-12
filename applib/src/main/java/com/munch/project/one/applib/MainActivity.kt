package com.munch.project.one.applib

import com.munch.lib.base.startActivity
import com.munch.lib.fast.base.BaseActivity
import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.fast.base.BaseRvActivity
import com.munch.project.one.applib.weight.FlowLayoutActivity

class MainActivity : BaseRvActivity() {

    override val targets: MutableList<Class<out BaseActivity>> =
        mutableListOf(WeightActivity::class.java)

    override fun canBack() = false
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