package com.munch.project.one.dev

import com.munch.lib.fast.base.BaseActivity
import com.munch.lib.fast.base.BaseRvActivity

class MainActivity : BaseRvActivity() {

    override val targets: MutableList<Class<out BaseActivity>> =
        mutableListOf(IntentActivity::class.java, AboutActivity::class.java)

    override fun canBack() = false
}

