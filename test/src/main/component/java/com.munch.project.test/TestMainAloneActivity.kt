package com.munch.project.test

import android.os.Bundle

/**
 * Create by munch1182 on 2020/12/7 13:58.
 */
class TestMainAloneActivity : TestMainActivity() {

    override fun notShowBack() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //用于测试首页启动时间，logcat Nofilters查看Fully drawn语句
        /*if (BaseApp.debugMode()) {
            reportFullyDrawn()
        }*/
        setTitle(R.string.test_main_title)
    }
}