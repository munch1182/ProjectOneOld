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
        ph?.handlePermissionRequestFirst()
    }

    fun start2Settings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        settingsResultLauncher.launch(intent)
        ResultHelper.log.log("forward to set")
    }
}

class PermissionHandler(
    private val fragment: InvisibleFragment,
    private val pb: ResultHelper.PermissionRequestBuilder
) {

    /**
     * 当前需要处理的权限列表
     */
    private val requestList = linkedSetOf<String>()

    /**
     * 当前已被允许的权限列表
     */
    private val grantedList = linkedSetOf<String>()

    /**
     * 当前申请被拒绝的权限列表
     */
    private val deniedNowList = linkedSetOf<String>()

    /**
     * 申请被拒绝且不再询问的权限列表
     */
    private val deniedPermanentList = linkedSetOf<String>()

    /**
     * 权限申请前的处理
     */
    fun handlePermissionRequestFirst() {
        pb.permissions.forEach { permission ->
            ResultHelper.log.log("handlePermissionRequestFirst :$permission -> ${isGrant(permission)}")
            if (isGrant(permission)) {
                grantedList.add(permission)
            } else {
                if (showRationale(permission)) {
                    deniedNowList.add(permission)
                } else if (!requestList.contains(permission)) {
                    requestList.add(permission)
                }
            }
        }

        val request = requestList.toTypedArray()
        //全部权限已获取，则结束流程
        if (request.isEmpty()) {
            ResultHelper.log.log("no need request")
            onFinish()
            return
        }
        //需要申请权限，且判断需要先解释权限
        if (pb.explainFirst && showPermissionDialogIfCan(request, true)) {
            return
        }
        //否则直接申请权限
        //@see [handlePermissionRequestResult]
        ResultHelper.log.log("request permission: [${requestList.joinToString()}]")
        fragment.normalPermissionLauncher.launch(request)
    }

    /**
     * 结束流程
     */
    private fun onFinish() {
        val allGrant = pb.permissions.size == grantedList.size
        val denied =
            ArrayList<String>(deniedNowList.size + deniedPermanentList.size + requestList.size)
        if (deniedNowList.size > 0) {
            denied.addAll(deniedNowList)
        }
        if (deniedPermanentList.size > 0) {
            denied.addAll(deniedPermanentList)
        }
        //当FirstDialog被取消时
        if (requestList.size > 0) {
            denied.addAll(requestList)
        }
        val granted = grantedList.toMutableList()
        ResultHelper.log.log("finish: allGrant:$allGrant, granted:[${granted.joinToString()}], denied:[${denied.joinToString()}]")
        pb.listener?.onResult(allGrant, granted, denied)
    }

    /**
     * 第一次权限申请后的回调的处理
     */
    fun handlePermissionRequestResult(permissionResult: Map<String, Boolean>) {
        for ((permission, granted) in permissionResult) {
            requestList.remove(permission)
            if (granted) {
                ResultHelper.log.log("handlePermissionRequestResult:$permission -> isGrant: true")
                grantedList.add(permission)
            } else {
                //拒绝但可以再次申请和提示
                if (showRationale(permission)) {
                    ResultHelper.log.log("handlePermissionRequestResult:$permission -> isGrant: false, showRationale: true")
                    deniedNowList.add(permission)
                    //权限被永久拒绝
                } else {
                    ResultHelper.log.log("handlePermissionRequestResult:$permission -> isGrant: false, showRationale: false")
                    deniedNowList.remove(permission)
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
        ResultHelper.log.log("show explain set dialog: [${permissions.joinToString()}]")
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
    private fun showPermissionDialogIfCan(
        permissions: Array<String>,
        isFirst: Boolean = false
    ): Boolean {
        if (permissions.isEmpty()) {
            return false
        }
        ResultHelper.log.log("show explain${if (isFirst) " first " else " "}dialog: [${permissions.joinToString()}]")
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
        val deniedPermanent = deniedPermanentList.toMutableList()
        deniedPermanent.forEach {
            if (isGrant(it)) {
                ResultHelper.log.log("handlePermissionRequestResultBackFromSettings: $it -> isGrant: true")
                deniedPermanentList.remove(it)
                grantedList.add(it)
            } else {
                ResultHelper.log.log("handlePermissionRequestResultBackFromSettings: $it -> isGrant: false")
            }
        }
        onFinish()
    }

    private fun isGrant(permission: String) =
        (ActivityCompat.checkSelfPermission(
            fragment.requireContext(), permission
        ) == PackageManager.PERMISSION_GRANTED)

    //当未申请过权限时，此方法返回false
    //当第一次拒绝时，此方法返回true
    //当被永久拒绝时，此方法返回false
    //所以无法在申请权限前就判断是否被永久拒绝
    private fun showRationale(permission: String) =
        fragment.shouldShowRequestPermissionRationale(permission)
}