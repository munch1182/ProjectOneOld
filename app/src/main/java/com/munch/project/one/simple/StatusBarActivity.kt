package com.munch.project.one.simple

import android.graphics.Color
import android.os.Bundle
import com.munch.lib.android.extend.bind
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.ActivityStatusBarBinding
import com.munch.project.one.other.load

/**
 * Create by munch1182 on 2022/9/22 16:36.
 */
class StatusBarActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private val bind by bind<ActivityStatusBarBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(bind.barTb)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true) }
        bind.barTb.navigationIcon?.setTint(Color.WHITE)
        bind.barImage.load("https://cn.bing.com/th?id=OHR.SpringPoint_ZH-CN6445792697_1080x1920.jpg&rf=LaDigue_1920x1080.jpg&pid=hp")
    }

}