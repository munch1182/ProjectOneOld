package com.munch.test.project.one.anim

import android.os.Bundle
import com.munch.lib.fast.databinding.ActivityBaseRvBinding
import com.munch.test.project.one.R

/**
 * Create by munch1182 on 2021/4/13 11:48.
 */
class SimpleTitleActivity : ShareElementActivity() {

    private val bind by bind<ActivityBaseRvBinding>(R.layout.activity_base_rv)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
    }
}