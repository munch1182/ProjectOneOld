package com.munch.lib.result.compat

import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

/**
 * Created by Munch on 2019/7/9 13:52
 */
class Permission {

    internal var fragment: Fragment? = null
    internal var permissions: Array<out String>? = null
    internal var grantResults: IntArray? = null
    internal var requestCode: Int? = null
    internal var permissionListener: PermissionListener? = null
    internal var loopListener: PermissionLoopListener? = null
    private var loop: Boolean = false

    /**
     * @param loop false：单次请求所有权限然后返回所有结果，直接回调{@link #result}
     *              true：请求单个权限后再请求下一个权限，回调{@link #loop}，全部请求完后回调{@link #result}
     *
     */
    fun isLoop(loop: Boolean): Permission {
        this.loop = loop
        return this
    }

    fun loop(listener: PermissionLoopListener): Permission {
        loopListener = listener
        return this
    }

    fun result(listener: PermissionListener) {
        this.permissionListener = listener
        request()
    }

    private fun request() {
        if (fragment != null && permissions != null && requestCode != null) {
            if (fragment!!.context == null) {
                return
            }
            if (loop) {
                permissions.forEachIndexed { index, s ->
                    if (ContextCompat.checkSelfPermission(
                            fragment!!.context!!, s
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (grantResults == null) {
                            grantResults = IntArray(permissions.size)
                        }
                        grantResults!![index] = PackageManager.PERMISSION_GRANTED
                    } else {
                        fragment!!.requestPermissions(arrayOf(s), requestCode!!)
                    }
                }
            } else {
                fragment!!.requestPermissions(permissions!!, requestCode!!)
            }
        }
    }

    fun onPermissionsResult(permissions: Array<out String>, grantResults: IntArray) {

    }
}