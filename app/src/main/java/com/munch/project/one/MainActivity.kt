package com.munch.project.one

import android.content.Intent
import android.os.Bundle
import com.munch.lib.android.extend.ctx
import com.munch.lib.fast.view.DataHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.dispatch.SupportActionBar
import com.munch.lib.fast.view.fastview.fvRvTv
import com.munch.plugin.annotation.Measure
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.dialog.DialogActivity

@Measure
class MainActivity : BaseActivity(), ActivityDispatch by SupportActionBar(false) {

    private val bind by fvRvTv(
        RecyclerViewActivity::class,
        PhoneInfoActivity::class,
        DialogActivity::class
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.init()
        DataHelper.firstPage?.let { startActivity(Intent(ctx, it)) }
    }
}

