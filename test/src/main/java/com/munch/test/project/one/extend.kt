package com.munch.test.project.one

import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX

/**
 * Create by munch1182 on 2021/4/8 13:35.
 */
fun FragmentActivity.request(vararg permission: String, action: () -> Unit) {
    PermissionX.init(this)
        .permissions(*permission)
        .request { allGranted, _, _ ->
            if (allGranted) {
                action.invoke()
            }
        }
}