package com.munch.project.one.simple

import android.os.Bundle
import com.munch.lib.android.helper.InfoHelper
import com.munch.lib.android.helper.NetHelper
import com.munch.lib.android.helper.getName
import com.munch.lib.android.helper.getNetAddress
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.fastview.fvSvTv
import com.munch.project.one.BuildConfig
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

class PhoneInfoActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private val bind by fvSvTv()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showInfo()
        NetHelper.observe(this) { showInfo() }
    }

    private fun showInfo() {
        val sb = StringBuilder()
        sb.append(InfoHelper.phoneDesc)
            .append("\n\n")
            .append(InfoHelper.appDesc).append(" ").append(BuildConfig.BUILD_TIME)
            .append("\n\n")
            .append(InfoHelper.windowDesc(this).replace(" ", "\n"))
            .append("\n\n")
            .append("curr net: ").append(NetHelper.curr?.getName() ?: "null")
            .append("\n")
            .append("address: ").append(getNetAddress() ?: "null")
        bind.set(sb)
    }
}

