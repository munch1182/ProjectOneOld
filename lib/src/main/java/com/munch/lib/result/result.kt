@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.result

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Create by munch1182 on 2022/4/15 22:03.
 */

inline fun FragmentActivity.permission(vararg permission: String) =
    ResultHelper.with(this).permission(*permission)

inline fun FragmentActivity.intent(intent: Intent) = ResultHelper.with(this).intent(intent)

inline fun FragmentActivity.judge(judge: OnJudge, intent: Intent) =
    ResultHelper.with(this).judge(judge, intent)

inline fun Fragment.permission(vararg permission: String) =
    ResultHelper.with(this).permission(*permission)

inline fun Fragment.intent(intent: Intent) = ResultHelper.with(this).intent(intent)

inline fun Fragment.judge(judge: OnJudge, intent: Intent) =
    ResultHelper.with(this).judge(judge, intent)

@Deprecated("?")
suspend inline fun FragmentActivity.permissionRequest(vararg permission: String) =
    suspendCancellableCoroutine<Boolean> {
        this.permission(*permission).request(object : OnPermissionResultListener {
            override fun onPermissionResult(
                isGrantAll: Boolean,
                grantedArray: Array<String>,
                deniedArray: Array<String>
            ) {
                it.resume(isGrantAll)
            }
        })
    }

@Deprecated("?")
suspend inline fun FragmentActivity.intentRequest(intent: Intent) =
    suspendCancellableCoroutine<Boolean> {
        this.intent(intent).start(object : OnIntentResultListener {
            override fun onIntentResult(isOk: Boolean, resultCode: Int, data: Intent?) {
                it.resume(isOk)
            }
        })
    }