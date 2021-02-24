package com.munch.project.launcher

import android.app.Activity
import android.os.Bundle
import com.munch.lib.helper.startActivity
import com.munch.project.launcher.app.AppActivity

/**
 * Create by munch1182 on 2021/2/23 14:41.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivity(AppActivity::class.java)
    }
}