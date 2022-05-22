package com.munch.lib.result.request

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.munch.lib.Resettable
import com.munch.lib.extend.isPermissionGranted
import com.munch.lib.extend.notDeniedForever
import com.munch.lib.log.LogStyle
import com.munch.lib.log.Logger
import com.munch.lib.result.OnPermissionResultListener

/**
 * Created by munch1182 on 2022/4/10 4:17.
 */
interface PermissionRequest {
    fun requestPermissions(permissions: Array<out String>, listener: OnPermissionResultListener?)
}

class PermissionRequestHandler(fragment: Fragment) : PermissionRequest, Resettable,
    ActivityResultCaller by fragment {

    private val log = Logger("intent", infoStyle = LogStyle.NONE)
    private val activity by lazy { fragment.requireActivity() }

    private val onResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

    private val normalPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

            log.log { "receive permission result." }
            it.forEach { m ->
                val element = m.key
                requestList.clear()
                if (m.value) {
                    grantedList.add(element)
                } else if (!activity.notDeniedForever(m.key)) {
                    deniedList.add(element)
                } else {
                    requestList.add(element)
                }
            }

            if (requestList.isEmpty()) {
                requestComplete()
            } else {
                // TODO: 显示dialog
                log.log { "launch permission again" }
                launchRequest()
            }
        }

    private fun launchRequest() {
        log.log { "launch permission." }
        normalPermissionLauncher.launch(requestList.toTypedArray())
    }

    private val settingsResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

    private var listener: OnPermissionResultListener? = null
    private val grantedList = mutableListOf<String>()
    private val requestList = mutableListOf<String>()
    private val deniedList = mutableListOf<String>()

    override fun requestPermissions(
        permissions: Array<out String>,
        listener: OnPermissionResultListener?
    ) {

        reset()
        this.listener = listener

        permissions.forEach {
            if (activity.isPermissionGranted(it)) {
                grantedList.add(it)
            } else {
                requestList.add(it)
            }
        }

        log.log {
            "request:${fmt(requestList)}, granted:${fmt(grantedList)}, denied:${fmt(deniedList)}"
        }

        if (requestList.isEmpty()) {
            requestComplete()
        } else {
            //todo 显示dialog
            launchRequest()
        }
    }

    private fun requestComplete() {
        log.log { "permission complete. granted: ${fmt(grantedList)}, denied:${fmt(deniedList)}" }
        listener?.onPermissionResult(
            deniedList.isEmpty(),
            grantedList.toTypedArray(),
            deniedList.toTypedArray()
        )
        reset()

        listener = null
    }

    override fun reset() {
        grantedList.clear()
        requestList.clear()
        deniedList.clear()
    }

    private fun fmt(list: MutableList<String>) = list.joinToString(prefix = "[", postfix = "]")
}