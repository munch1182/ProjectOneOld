@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.munch.lib.AppHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Create by munch1182 on 2022/4/15 22:03.
 */

inline fun FragmentActivity.permission(vararg permission: String) =
    ResultHelper.with(this).permission(*permission)

inline fun Fragment.permission(vararg permission: String) =
    ResultHelper.with(this).permission(*permission)

inline fun FragmentActivity.intent(intent: Intent) = ResultHelper.with(this).intent(intent)

inline fun Fragment.intent(intent: Intent) = ResultHelper.with(this).intent(intent)

fun FragmentActivity.judgeIntent(onJudge: OnJudge, intent: OnIntent) =
    ResultHelper.with(this).judgeOrIntent(onJudge, intent)

fun Fragment.judgeIntent(onJudge: OnJudge, intent: OnIntent) =
    ResultHelper.with(this).judgeOrIntent(onJudge, intent)

suspend fun PermissionRequest.isGrantAll(): Boolean {
    return suspendCancellableCoroutine { request { isGrantAll, _ -> it.resume(isGrantAll) } }
}

inline fun FragmentActivity.contact(vararg permission: String) =
    ResultHelper.with(this).contact(*permission)

inline fun Fragment.contact(vararg permission: String) =
    ResultHelper.with(this).contact(*permission)

inline fun FragmentActivity.contact(intent: Intent) = ResultHelper.with(this).contact(intent)

inline fun Fragment.contact(intent: Intent) = ResultHelper.with(this).contact(intent)

fun FragmentActivity.contact(onJudge: OnJudge, intent: OnIntent) =
    ResultHelper.with(this).contact(onJudge, intent)

fun Fragment.contact(onJudge: OnJudge, intent: OnIntent) =
    ResultHelper.with(this).contact(onJudge, intent)

inline fun String.isGranted(context: Context = AppHelper.app): Boolean {
    return PermissionChecker.checkSelfPermission(context, this) ==
            PermissionChecker.PERMISSION_GRANTED
}

inline fun String.isDeniedForever(activity: Activity): Boolean {
    return !ActivityCompat.shouldShowRequestPermissionRationale(activity, this)
}