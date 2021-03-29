package com.munch.project.launcher.set

import android.os.Bundle
import com.munch.lib.helper.clickItem
import com.munch.project.launcher.R
import com.munch.project.launcher.app.task.AppItemHelper
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.databinding.ActivitySetBinding

/**
 * Create by munch1182 on 2021/3/2 10:58.
 */
class SettingActivity : BaseActivity() {

    private val bind by bind<ActivitySetBinding>(R.layout.activity_set)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        bind.setContainer.clickItem({
            if (it.tag !is Int) {
                return@clickItem
            }
            when (it.tag) {
                0 -> {
                    AppItemHelper.getInstance().changeShape(0)
                }
            }
        })
    }

}