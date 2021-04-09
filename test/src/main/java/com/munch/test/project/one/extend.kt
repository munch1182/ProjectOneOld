package com.munch.test.project.one

import androidx.fragment.app.Fragment
import com.munch.lib.fast.base.activity.BaseTopActivity
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

fun Fragment.requestPermission(vararg permission: String, action: () -> Unit) {
    PermissionX.init(this)
        .permissions(*permission)
        .request { allGranted, _, _ ->
            if (allGranted) {
                action.invoke()
            } else {
                if (activity is BaseTopActivity) {
                    (activity as BaseTopActivity).toast("有权限未获得")
                }
            }
        }
}