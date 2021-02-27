package com.munch.project.launcher

import android.os.Bundle
import android.widget.Button
import com.munch.project.launcher.appitem.AppActivity
import com.munch.project.launcher.base.BaseActivity

/**
 * Create by munch1182 on 2021/2/23 14:41.
 */
class MainActivity : BaseActivity() {

    private val btnApp: Button by lazy { findViewById(R.id.main_btn_app) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnApp.setOnClickListener {
            AppActivity.start(this)
        }
    }
}