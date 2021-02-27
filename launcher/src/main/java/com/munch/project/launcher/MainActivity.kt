package com.munch.project.launcher

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.munch.project.launcher.appitem.AppActivity
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.databinding.ActivityMainBinding

/**
 * Create by munch1182 on 2021/2/23 14:41.
 */
class MainActivity : BaseActivity() {

    private val bind by bind<ActivityMainBinding>(R.layout.activity_main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this

        bind.mainBtnApp.setOnClickListener {
            AppActivity.start(this)
        }
    }

    override fun setPage(view: View) {
        super.setPage(view)
        window.navigationBarColor = Color.TRANSPARENT
    }
}