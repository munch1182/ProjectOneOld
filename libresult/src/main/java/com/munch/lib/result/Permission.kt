package com.munch.lib.result

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

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
    private var index = -1

    /**
     *
     * 循环请求所有权限，否则一次请求全部权限
     * @param loop false：单次请求所有权限然后返回所有结果，直接回调{@link #loop}
     *              true：请求单个权限后再请求下一个权限，回调{@link #loop}，全部请求完后回调{@link #loop}
     *
     */
    fun isLoop(loop: Boolean): Permission {
        this.loop = loop
        return this
    }

    /**
     * @param listener 返回false则继续判断请求下一个权限，否则结束整个权限请求
     */
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
            if (grantResults == null) {
                grantResults = IntArray(permissions!!.size) { PackageManager.PERMISSION_DENIED }
            }
            if (loop) {
                index++
                val s = permissions!![index]
                if (ContextCompat.checkSelfPermission(
                        fragment!!.context!!, s
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    grantResults!![index] = PackageManager.PERMISSION_GRANTED
                } else {
                    fragment!!.requestPermissions(arrayOf(s), requestCode!!)
                }
            } else {
                fragment!!.requestPermissions(permissions!!, requestCode!!)
            }
        }
    }

    fun onPermissionsResult(permissions: Array<out String>, grantResults: IntArray) {
        if (loop) {
            if (this.index != -1) {
                this.grantResults!![index] = grantResults[0]
                if (loopListener?.loop(permissions[0], grantResults[0]) != false) {
                    permissionListener?.result(this.permissions!!, this.grantResults!!)
                    return
                }
                if (index == this.permissions!!.size - 1) {
                    permissionListener?.result(this.permissions!!, this.grantResults!!)
                } else {
                    request()
                }
            }
        } else {
            permissionListener?.result(permissions, grantResults)
        }
    }
}