@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.result

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Create by munch1182 on 2022/4/15 22:03.
 */

inline fun FragmentActivity.permission(vararg permission: String) =
    ResultHelper.with(this).permission(*permission)

inline fun Fragment.permission(vararg permission: String) =
    ResultHelper.with(this).permission(*permission)

inline fun FragmentActivity.intent(intent: Intent) = ResultHelper.with(this).intent(intent)
inline fun Fragment.intent(intent: Intent) = ResultHelper.with(this).intent(intent)

inline fun FragmentActivity.judgeIntent(noinline onJudge: OnJudge, intent: Intent) =
    ResultHelper.with(this).judgeOrIntent(onJudge, intent)

inline fun Fragment.judgeIntent(noinline onJudge: OnJudge, intent: Intent) =
    ResultHelper.with(this).judgeOrIntent(onJudge, intent)

inline fun ResultHelper.IntentRequest.start(crossinline f: (isOk: Boolean, data: Intent?) -> Unit) =
    start(object : OnIntentResultListener {
        override fun onIntentResult(isOk: Boolean, data: Intent?) {
            f.invoke(isOk, data)
        }
    })

inline fun ResultHelper.JudgeOrIntentRequest.start(crossinline f: (isOk: Boolean) -> Unit) =
    start(object : OnJudgeResultListener {
        override fun onJudgeResult(result: Boolean) {
            f.invoke(result)
        }
    })

inline fun ResultHelper.PermissionRequest.request(crossinline r: (isGrantAll: Boolean, result: Map<String, Boolean>) -> Unit) {
    request(object : OnPermissionResultListener {
        override fun onPermissionResult(isGrantAll: Boolean, result: Map<String, Boolean>) {
            r.invoke(isGrantAll, result)
        }
    })
}