package com.munch.project.one

import android.os.Bundle
import com.munch.lib.android.helper.InfoHelper
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.fvSvTv
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