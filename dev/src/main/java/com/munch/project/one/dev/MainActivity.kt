package com.munch.project.one.dev

import android.os.Bundle
import com.munch.lib.base.startActivity
import com.munch.lib.fast.base.BaseActivity
import com.munch.lib.fast.base.BaseRvActivity
import com.munch.lib.fast.base.DataHelper
import com.munch.project.one.dev.test.TestActivity

class MainActivity : BaseRvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firstActivity = DataHelper.firstActivity
        if (firstActivity != null) {
            startActivity(firstActivity)
            return
        }
    }

    override val targets: MutableList<Class<out BaseActivity>> =
        mutableListOf(
            IntentActivity::class.java,
            AboutActivity::class.java,
            TestActivity::class.java
        )

    override fun canBack() = false

    override fun showNotice() {
        //禁止循环跳转
        /*super.showNotice()*/
    }
}

