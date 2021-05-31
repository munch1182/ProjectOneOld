package com.munch.test.project.one

import com.munch.lib.fast.base.activity.BaseTopActivity
import com.munch.test.project.one.base.BaseFragment
import com.permissionx.guolindev.PermissionX

/**
 * Create by munch1182 on 2021/4/8 13:35.
 */
fun BaseTopActivity.requestPermission(vararg permission: String, action: () -> Unit) {
    PermissionX.init(this)
        .permissions(*permission)
        .request { allGranted, _, _ ->
            if (allGranted) {
                action.invoke()
            } else {
                toast("有权限未获得")
            }
        }
}

fun BaseFragment.requestPermission(vararg permission: String, action: () -> Unit) {
    PermissionX.init(this)
        .permissions(*permission)
        .request { allGranted, _, _ ->
            if (allGranted) {
                action.invoke()
            } else {
                toast("有权限未获得")
            }
        }
}