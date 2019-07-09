package com.munch.lib.result.compat

/**
 * Created by Munch on 2019/7/9 17:30
 */
interface PermissionLoopListener {

    fun result(permissions: String, grantResults: Int): Boolean
}