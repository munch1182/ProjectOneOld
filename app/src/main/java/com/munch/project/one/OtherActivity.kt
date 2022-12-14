package com.munch.project.one

import android.os.Bundle
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.fastview.fvRvTv
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.simple.BluetoothHarmonyOSActivity


/**
 * Create by munch1182 on 2022/12/14 17:41.
 */
class OtherActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private val bind by fvRvTv(
        BluetoothHarmonyOSActivity::class
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind
    }
}