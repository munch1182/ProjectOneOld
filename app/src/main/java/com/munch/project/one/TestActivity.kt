package com.munch.project.one

import android.os.Bundle
import com.munch.lib.android.extend.bind
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.LayoutActBinding

/**
 * Create by munch on 2022/9/18 4:24.
 */
class TestActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private val bind by bind<LayoutActBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.fl.update {
            maxCountInLine = 0
        }
    }
}