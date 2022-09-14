package com.munch.project.one

import android.os.Bundle
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.fvRvTv
import com.munch.plugin.annotation.Measure
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.dialog.DialogActivity

@Measure
class MainActivity : BaseActivity(), ActivityDispatch by dispatchDef(false) {

    private val bind by fvRvTv(
        RecyclerViewActivity::class,
        PhoneInfoActivity::class,
        DialogActivity::class
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.init()
    }
}

