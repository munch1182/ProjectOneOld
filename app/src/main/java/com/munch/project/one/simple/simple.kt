package com.munch.project.one.simple

import android.os.Bundle
import com.munch.lib.android.helper.InfoHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.fastview.fvSvTv
import com.munch.project.one.BuildConfig
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

class PhoneInfoActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private val bind by fvSvTv()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.set(
            "${InfoHelper.phoneDesc}\n\n" +
                    "${InfoHelper.appDesc} ${BuildConfig.BUILD_TIME}\n\n" +
                    InfoHelper.windowDesc.replace(" ", "\n")
        )
    }
}

