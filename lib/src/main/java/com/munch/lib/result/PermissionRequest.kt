package com.munch.lib.result

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.collection.ArrayMap
import com.munch.lib.log.Logger
import com.munch.lib.notice.Notice
import com.munch.lib.notice.choseSureOrFalse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PermissionRequest(
    private val permissions: Array<out String>,
    private val fragment: ResultFragment
) : ResultHelper.IRequest {
    private val log = Logger("result")
    private var explain: ExplainPermissionNotice? = null
    private val granted = mutableListOf<String>()
    private val denied = mutableListOf<String>()
    private val request = mutableListOf<String>()
    private val map = ArrayMap<String, Boolean>()

    fun explain(explain: (Context) -> ExplainPermissionNotice): PermissionRequest {
        this.explain = explain.invoke(fragment.requireContext())
        return this
    }

    fun request(listener: OnPermissionResultListener?) {
        permissions.forEach {
            //权限未授予的未必是被拒绝
            val isGranted = it.isGranted()
            if (isGranted) granted.add(it) else request.add(it)
            map[it] = isGranted
        }
        // 全部权限已获取
        if (!map.any { !it.value }) {
            log.log { "permission granted all." }
            listener?.onPermissionResult(true, map)
            return
        }
        fragment.launch(Dispatchers.Default) {
            //处理notice
            val firstChose = choseFromRequestNotice(true, map)
            //如果解释权限后就被拒绝，则直接回调
            if (!firstChose) {
                log.log { "first notice denied." }
                listener?.onPermissionResult(false, map)
                return@launch
            }
            //开始请求权限
            startPermissionRequest(request.toTypedArray())
            //在请求后，即可正式判断
            checkPermission()
            //先请求未被完全拒绝的权限
            if (request.isNotEmpty()) {
                log.log { "permission judge request again for request." }
                //处理notice
                val requestAgain = choseFromRequestNotice(false, map)
                //如果解释后不请求权限，则结束
                if (!requestAgain) {
                    log.log { "permission request again denied." }
                    listener?.onPermissionResult(false, map)
                    return@launch
                }
                log.log { "permission request again." }
                // 再次请求未被拒绝的权限
                startPermissionRequest(request.toTypedArray())
                checkPermission()
            }
            if (denied.isNotEmpty()) {
                log.log { "permission judge go to set for denied." }
                val toSet = choseFromSetNotice(denied.toTypedArray())
                if (!toSet) {
                    log.log { "permission go to set denied." }
                    listener?.onPermissionResult(false, map)
                    return@launch
                }
                log.log { "permission go to set." }
                //跳转设置界面
                toSetRequest()
                checkPermission()
            }
            val isGrantAll = granted.size == map.size
            log.log { "permission result: $isGrantAll." }
            listener?.onPermissionResult(isGrantAll, map)
        }
    }

    private fun checkPermission() {
        clear()
        permissions.forEach {
            if (it.isGranted()) {
                granted.add(it)
                map[it] = true
            } else if (it.isDeniedForever(fragment.requireActivity())) {
                denied.add(it)
                map[it] = false
            } else {
                request.add(it)
                map[it] = false
            }
        }
    }

    private suspend fun toSetRequest(): Boolean {
        return suspendCancellableCoroutine {
            val ctx = fragment.requireContext()
            IntentRequest(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${ctx.packageName}")
                ), fragment
            ).start { isOk, _ -> it.resume(isOk) }
        }
    }

    private suspend fun startPermissionRequest(permissions: Array<String>): Boolean {
        return suspendCancellableCoroutine {
            fragment.requestPermissions(permissions) { isGrantAll, _ -> it.resume(isGrantAll) }
        }
    }

    private suspend fun choseFromRequestNotice(
        isBeforeRequest: Boolean,
        map: Map<String, Boolean>
    ): Boolean {
        val e = explain
        if (e == null || !e.onPermissionExplain(isBeforeRequest, map)) {
            return true
        }
        return e.choseSureOrFalse()
    }

    private suspend fun choseFromSetNotice(denied: Array<String>): Boolean {
        val e = explain
        if (e == null || !e.onToSetExplain(denied)) {
            return true
        }
        return e.choseSureOrFalse()
    }

    private fun clear() {
        request.clear()
        granted.clear()
        denied.clear()
        map.clear()
    }

}

interface ExplainPermissionNotice : Notice {


    /**
     * 当需要解释权限请求时会回调此方法
     *
     * 此方法是用于调整显示
     *
     * @param isBeforeRequest 是否是在请求权限之前的回调
     * @param permissions 当前的权限
     *
     * @return 是否显示
     */
    fun onPermissionExplain(isBeforeRequest: Boolean, permissions: Map<String, Boolean>): Boolean

    /**
     * 当需要解释前往设置界面时会回调此方法
     *
     * 此方法是用于调整显示
     *
     * @param denied 已经被永久拒绝的权限
     *
     * @return 是否显示
     */
    fun onToSetExplain(denied: Array<String>): Boolean = false
}

fun interface OnPermissionResultListener {

    fun onPermissionResult(isGrantAll: Boolean, result: Map<String, Boolean>)
}