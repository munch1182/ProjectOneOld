package com.munch.project.one.applib.net

import android.os.Bundle
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.base.get
import com.munch.project.one.applib.R
import com.munch.project.one.applib.databinding.ActivityNetBinding

/**
 * Create by munch1182 on 2021/9/1 16:50.
 */
class TestNetActivity : BaseBigTextTitleActivity() {


    private val bind by bind<ActivityNetBinding>(R.layout.activity_net)
    private val vm by get(TestNetViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            vm = this@TestNetActivity.vm
        }
    }
}