package com.munch.project.one

import android.os.Bundle
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.fvRvTv
import com.munch.plugin.annotation.Measure
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

@Measure
class MainActivity : BaseActivity(), ActivityDispatch by dispatchDef(false) {

    private val bind by fvRvTv(
        arrayOf(
            RecyclerViewActivity::class,
            PhoneInfoActivity::class,
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.init()
    }
}

