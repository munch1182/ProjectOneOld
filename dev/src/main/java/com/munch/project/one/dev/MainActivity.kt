package com.munch.project.one.dev

import com.munch.lib.fast.base.BaseActivity
import com.munch.lib.fast.base.BaseRvActivity
import com.munch.project.one.dev.test.TestActivity

class MainActivity : BaseRvActivity() {

    override val targets: MutableList<Class<out BaseActivity>> =
        mutableListOf(
            IntentActivity::class.java,
            AboutActivity::class.java,
            TestActivity::class.java
        )

    override fun canBack() = false
}

