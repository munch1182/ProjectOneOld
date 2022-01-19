package com.munch.lib.base

import android.content.Context
import androidx.core.content.PermissionChecker

/**
 * 用于处理带权限的操作，比如监听、操作
 *
 * Create by munch1182 on 2022/1/19 11:21.
 */
interface PermissionHandler {

    val permissions: Array<String>

    val context: Context

    /**
     * 检查是否有权限
     * 一般除了[permissions]外，还可能有需要用其它方式判断的权限，需要在此处一并判断
     */
    fun hadPermissions(): Boolean {
        permissions.forEach {
            if (!hadPermission(context, it)) {
                return false
            }
        }
        return true
    }

    /**
     * 申请权限，包括所有方式的权限
     * 请求成功后需要自行回调[onPermissionGranted]
     */
    fun requestPermission() {}

    fun hadPermission(context: Context, permission: String): Boolean {
        return PermissionChecker.checkSelfPermission(
            context, permission
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    fun start() {
        if (hadPermissions()) {
            onPermissionGranted()
        } else {
            requestPermission()
        }
    }

    fun onPermissionGranted() {}
}