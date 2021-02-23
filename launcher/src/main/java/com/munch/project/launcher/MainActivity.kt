package com.munch.project.launcher

import android.os.Bundle
import com.munch.lib.helper.startActivity
import com.munch.project.launcher.app.AppActivity
import com.munch.project.launcher.base.BaseActivity

/**
 * Create by munch1182 on 2021/2/23 14:41.
 */
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivity(AppActivity::class.java)
    }
}