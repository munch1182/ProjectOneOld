package com.munch.lib.result.request

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.munch.lib.base.Resettable
import com.munch.lib.extend.isPermissionGranted
import com.munch.lib.result.OnPermissionResultListener

/**
 * Created by munch1182 on 2022/4/10 4:17.
 */
interface PermissionRequest {
    fun requestPermissions(permissions: Array<out String>, listener: OnPermissionResultListener)
}

class PermissionRequestHandler(fragment: Fragment) : PermissionRequest, Resettable,
    ActivityResultCaller by fragment {

    private val activity by lazy { fragment.requireActivity() }

    private val onResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

    private val normalPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            it.forEach { m ->
                val element = m.key
                requestList.remove(element)
                if (m.value) {
                    grantedList.add(element)
                } else {
                    deniedList.add(element)
                }
            }
            listener?.onPermissionResult(
                deniedList.isEmpty(),
                grantedList.toTypedArray(),
                deniedList.toTypedArray()
            )
            reset()
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
        listener: OnPermissionResultListener
    ) {

        reset()

        permissions.forEach {
            if (activity.isPermissionGranted(it)){
                grantedList.add(it)
            }else{
                requestList.add(it)
            }
        }

        if (requestList.isEmpty() ) {
            requestComplete()
        } else {
            this.listener = listener
            dispatchPermissionRequest()
        }
    }

    private fun dispatchPermissionRequest() {
        normalPermissionLauncher.launch(requestList.toTypedArray())
    }

    private fun requestComplete() {
        listener?.onPermissionResult(true, grantedList.toTypedArray(), deniedList.toTypedArray())
        reset()
    }

    override fun reset() {
        grantedList.clear()
        requestList.clear()
        deniedList.clear()
        listener = null
    }
}