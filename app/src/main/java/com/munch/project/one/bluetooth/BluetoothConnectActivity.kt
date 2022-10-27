package com.munch.project.one.bluetooth

import android.os.Bundle
import com.munch.lib.android.extend.get
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

/**
 * Create by munch1182 on 2022/10/27 17:21.
 */
class BluetoothConnectActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private val vm by get<BluetoothVM>(BluetoothVM.SHARE_NAME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.state.observe(this) {

        }
    }

}