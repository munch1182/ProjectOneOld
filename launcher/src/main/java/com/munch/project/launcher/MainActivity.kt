package com.munch.project.launcher

import android.os.Bundle
import com.munch.lib.android.extend.bind
import com.munch.lib.android.extend.replace
import com.munch.project.launcher.databinding.ActivityMainBinding

/**
 * Create by munch1182 on 2022/4/3 16:35.
 */
class MainActivity : BaseActivity() {

    private val bind by bind<ActivityMainBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.root
        replace(R.id.fl, HomeFragment())
    }
}