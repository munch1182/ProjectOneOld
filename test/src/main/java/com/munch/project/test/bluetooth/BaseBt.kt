package com.munch.project.test.bluetooth

import com.munch.lib.base.BaseRootActivity
import com.munch.lib.bt.BluetoothHelper
import com.permissionx.guolindev.PermissionX

/**
 * Create by munch1182 on 2021/3/16 17:26.
 */
interface BaseBt {

    fun checkBt(func: () -> Unit) {
        PermissionX.init(getContext())
            .permissions(*BluetoothHelper.permissions())
            .request { allGranted, _, _ ->
                if (!allGranted) {
                    getContext().toast("拒绝了权限")
                } else {
                    if (BluetoothHelper.getInstance().open()) {
                        func.invoke()
                    } else {
                        getContext().toast("蓝牙开启失败")
                    }
                }
            }
    }

    fun getContext(): BaseRootActivity
}