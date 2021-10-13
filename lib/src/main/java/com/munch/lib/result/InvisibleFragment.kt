package com.munch.lib.result

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.munch.lib.base.UnKnowException

/**
 * Create by munch1182 on 2021/8/20 16:37.
 */
class InvisibleFragment : Fragment() {

    companion object {
        const val TAG = "com.munch.lib.result.InvisibleFragment"
    }

    private var resultListener: ResultHelper.OnActivityResultListener? = null
    private var ph: PermissionHandler? = null

    private val onResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            resultListener?.onResult(it.resultCode == Activity.RESULT_OK, it.resultCode, it.data)
        }

    internal val normalPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            ph?.handlePermissionRequestResult(it)
        }

    private val settingsResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            ph?.handlePermissionRequestResultBackFromSettings()
        }

    fun startActivityForResult(intent: Intent, listener: ResultHelper.OnActivityResultListener) {
        resultListener = listener
        onResultLauncher.launch(intent)
    }

    fun requestPermissions(pb: ResultHelper.PermissionRequestBuilder) {
        ph = PermissionHandler(this, pb)
        ph?.handlePermissionRequest()
    }

    fun start2Settings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        settingsResultLauncher.launch(intent)
    }
}

class PermissionHandler(
    private val fragment: InvisibleFragment,
    private val pb: ResultHelper.PermissionRequestBuilder
) {

    /**
     * 当前需要处理的权限列表
     */
    private val requestList = mutableListOf<String>()

    /**
     * 当前已被允许的权限列表
     */
    private val grantedList = mutableListOf<String>()

    /**
     * 当前申请被拒绝的权限列表
     */
    private val deniedNowList = mutableListOf<String>()

    /**
     * 申请被拒绝且不再询问的权限列表
     */
    private val deniedPermanentList = mutableListOf<String>()

    /**
     * 权限申请前的处理
     */
    fun handlePermissionRequest() {
        pb.permissions.forEach {
            if (isGrant(it)) {
                grantedList.add(it)
            } else {
                requestList.add(it)
            }
        }

        val request = requestList.toTypedArray()
        //全部权限已获取，则结束流程
        if (request.isEmpty()) {
            onFinish()
            return
        }
        //需要申请权限，且判断需要先解释权限
        if (pb.explainFirst && showPermissionDialogIfCan(request)) {
            return
        }
        //否则直接申请权限
        //@see [handlePermissionRequestResult]
        fragment.normalPermissionLauncher.launch(request)
    }

    /**
     * 结束流程
     */
    private fun onFinish() {
        val allGrant = pb.permissions.size == grantedList.size
        val deniedList = ArrayList<String>(deniedNowList.size + deniedPermanentList.size)
        if (deniedNowList.size > 0) {
            deniedList.addAll(deniedNowList)
        }
        if (deniedPermanentList.size > 0) {
            deniedList.addAll(deniedPermanentList)
        }
        pb.listener?.onResult(allGrant, grantedList, deniedList)
    }

    /**
     * 第一次权限申请后的回调的处理
     */
    fun handlePermissionRequestResult(permissionResult: Map<String, Boolean>) {
        for ((permission, granted) in permissionResult) {
            requestList.remove(permission)
            if (granted) {
                grantedList.add(permission)
            } else {
                val shouldShowRationale = fragment.shouldShowRequestPermissionRationale(permission)
                //拒绝但可以再次申请和提示
                if (shouldShowRationale) {
                    deniedNowList.add(permission)
                    //权限被永久拒绝
                } else {
                    deniedPermanentList.add(permission)
                }
            }
        }
        //此时requestList应该为空
        if (requestList.isNotEmpty()) {
            throw UnKnowException()
        }

        //当前拒绝权限执行解释和再次申请
        val permissions = deniedNowList.toTypedArray()
        if (showPermissionDialogIfCan(permissions)) {
            return
        }
        if (showExplainSetDialogIfCan()) {
            return
        }

        onFinish()
    }

    private fun showExplainSetDialogIfCan(): Boolean {
        //被永久拒绝权限执行解释和跳转
        val permissions = deniedPermanentList.toTypedArray()
        if (permissions.isEmpty()) {
            return false
        }
        val dialog = pb.explainDialogNeed2Setting?.invoke(fragment.requireContext(), permissions)
        if (dialog != null) {
            dialog.setOnNext { fragment.start2Settings() }
                .setOnCancel { onFinish() }
                .show()
            return true
        }
        return false
    }

    /**
     * 是否显示权限解释Dialog
     */
    private fun showPermissionDialogIfCan(permissions: Array<String>): Boolean {
        if (permissions.isEmpty()) {
            return false
        }
        val dialog = pb.explainDialog?.invoke(fragment.requireContext(), permissions)
        if (dialog != null) {
            dialog.setOnNext { fragment.normalPermissionLauncher.launch(permissions) }
                .setOnCancel { onFinish() }
                .show()
            return true
        }
        return false
    }

    /**
     * 从设置界面返回后的权限判断处理
     */
    fun handlePermissionRequestResultBackFromSettings() {
        val deniedPermanent = MutableList(deniedPermanentList.size) { deniedPermanentList[it] }
        deniedPermanent.forEach {
            if (isGrant(it)) {
                deniedPermanentList.remove(it)
                grantedList.add(it)
            }
        }
        onFinish()
    }

    private fun isGrant(permission: String) =
        (ActivityCompat.checkSelfPermission(
            fragment.requireContext(), permission
        ) == PackageManager.PERMISSION_GRANTED)
}