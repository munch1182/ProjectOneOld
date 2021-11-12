package com.munch.project.one.weight

import android.os.Bundle
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.project.one.databinding.ActivityLoadingBinding

/**
 * Create by munch1182 on 2021/11/12 11:40.
 */
class LoadingActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityLoadingBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.let {  }
    }
}