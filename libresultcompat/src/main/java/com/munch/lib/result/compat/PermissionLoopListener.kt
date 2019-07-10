package com.munch.lib.result.compat

/**
 * Created by Munch on 2019/7/9 17:30
 */
interface PermissionLoopListener {

    fun loop(permissions: String, grantResults: Int): Boolean
}