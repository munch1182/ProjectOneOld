@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.result

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Create by munch1182 on 2021/11/29 13:45.
 */

inline fun FragmentActivity.with(vararg permission: String) =
    ResultHelper.init(this).with(*permission)

inline fun FragmentActivity.with(minVersion: Int, vararg permission: String) =
    ResultHelper.init(this).with(minVersion, *permission)

inline fun FragmentActivity.with(intent: Intent) = ResultHelper.init(this).with(intent)
inline fun FragmentActivity.with(noinline judge: () -> Boolean, intent: Intent) =
    ResultHelper.init(this).with(judge, intent)

inline fun FragmentActivity.contactWith(vararg permission: String) =
    ResultHelper.init(this).contactWith(*permission)

inline fun FragmentActivity.contactWith(minVersion: Int, vararg permission: String) =
    ResultHelper.init(this).contactWith(minVersion, *permission)

inline fun FragmentActivity.contactWith(intent: Intent) =
    ResultHelper.init(this).contactWith(intent)

inline fun FragmentActivity.contactWith(noinline judge: () -> Boolean, intent: Intent) =
    ResultHelper.init(this).contactWith(judge, intent)

inline fun Fragment.with(vararg permission: String) = ResultHelper.init(this).with(*permission)
inline fun Fragment.with(intent: Intent) = ResultHelper.init(this).with(intent)
inline fun Fragment.with(noinline judge: () -> Boolean, intent: Intent) =
    ResultHelper.init(this).with(judge, intent)

inline fun Fragment.contactWith(vararg permission: String) =
    ResultHelper.init(this).contactWith(*permission)

inline fun Fragment.contactWith(minVersion: Int, vararg permission: String) =
    ResultHelper.init(this).contactWith(minVersion, *permission)

inline fun Fragment.contactWith(intent: Intent) = ResultHelper.init(this).contactWith(intent)
inline fun Fragment.contactWith(noinline judge: () -> Boolean, intent: Intent) =
    ResultHelper.init(this).contactWith(judge, intent)

inline fun Activity.hasPermission(vararg permission: String): Boolean {
    permission.forEach {
        if (checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}