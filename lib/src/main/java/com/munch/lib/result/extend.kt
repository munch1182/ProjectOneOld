package com.munch.lib.result

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Create by munch1182 on 2021/11/29 13:45.
 */

fun FragmentActivity.with(vararg permission: String) = ResultHelper.init(this).with(*permission)
fun FragmentActivity.with(intent: Intent) = ResultHelper.init(this).with(intent)
fun FragmentActivity.with(judge: () -> Boolean, intent: Intent) =
    ResultHelper.init(this).with(judge, intent)

fun FragmentActivity.contactWith(vararg permission: String) =
    ResultHelper.init(this).contactWith(*permission)

fun FragmentActivity.contactWith(intent: Intent) = ResultHelper.init(this).contactWith(intent)
fun FragmentActivity.contactWith(judge: () -> Boolean, intent: Intent) =
    ResultHelper.init(this).contactWith(judge, intent)

fun Fragment.with(vararg permission: String) = ResultHelper.init(this).with(*permission)
fun Fragment.with(intent: Intent) = ResultHelper.init(this).with(intent)
fun Fragment.with(judge: () -> Boolean, intent: Intent) =
    ResultHelper.init(this).with(judge, intent)

fun Fragment.contactWith(vararg permission: String) =
    ResultHelper.init(this).contactWith(*permission)

fun Fragment.contactWith(intent: Intent) = ResultHelper.init(this).contactWith(intent)
fun Fragment.contactWith(judge: () -> Boolean, intent: Intent) =
    ResultHelper.init(this).contactWith(judge, intent)

fun Activity.hasPermission(vararg permission: String): Boolean {
    permission.forEach {
        if (checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}