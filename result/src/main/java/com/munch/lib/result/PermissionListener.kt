package com.munch.lib.result

/**
 * Created by Munch on 2019/7/9 13:53
 */
interface PermissionListener {

    fun result(permissions: Array<out String>, grantResults: IntArray)
}