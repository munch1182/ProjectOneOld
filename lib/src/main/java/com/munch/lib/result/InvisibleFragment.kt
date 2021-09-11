package com.munch.lib.result

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import java.util.concurrent.atomic.AtomicInteger

/**
 * Create by munch1182 on 2021/8/20 16:37.
 */
class InvisibleFragment : Fragment() {

    companion object {
        const val TAG = "com.munch.lib.result.InvisibleFragment"
    }

    private val requestCode = AtomicInteger(0)
    private val permissionMap: SparseArray<ResultHelper.OnPermissionResultListener> = SparseArray(2)
    private val resultMap: SparseArray<ResultHelper.OnActivityResultListener> = SparseArray(2)
    private val triggeredMap: SparseArray<(() -> Unit)> = SparseArray(2)

    /**
     * 用于跳转其它页面后回到此页的回调
     */
    private var onTriggeredListener: (() -> Unit)? = null

    private fun newRequestCode() = requestCode.incrementAndGet()

    fun setOnTriggeredListener(
        requestCode: Int,
        listener: (() -> Unit)
    ): InvisibleFragment {
        triggeredMap.put(requestCode, listener)
        return this
    }

    fun removeOnTriggeredListener(requestCode: Int) {
        triggeredMap.remove(requestCode)
    }

    override fun onResume() {
        super.onResume()
        triggeredMap.forEach { _, value -> value.invoke() }
    }

    fun startActivityForResult(
        intent: Intent?,
        callback: ResultHelper.OnActivityResultListener
    ) {
        val requestCode = newRequestCode()
        resultMap.put(requestCode, callback)
        @Suppress("DEPRECATION")
        super.startActivityForResult(intent, requestCode)
    }

    fun requestPermissions(
        permissions: Array<out String>,
        callback: ResultHelper.OnPermissionResultListener
    ) {
        val requestCode = newRequestCode()
        permissionMap.put(requestCode, callback)
        @Suppress("DEPRECATION")
        super.requestPermissions(permissions, requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissionMap.indexOfKey(requestCode) == -1) {
            return
        }

        val grantList = ArrayList<String>()
        val delayList = ArrayList<String>()
        grantResults.forEachIndexed { index, i ->
            if (i == PackageManager.PERMISSION_GRANTED) {
                grantList.add(permissions[index])
            } else {
                delayList.add(permissions[index])
            }
        }
        permissionMap[requestCode].onResult(delayList.isEmpty(), grantList, delayList)
        permissionMap.remove(requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultMap.indexOfKey(requestCode) == -1) {
            return
        }

        resultMap[requestCode].onResult(resultCode == Activity.RESULT_OK, resultCode, data)
        resultMap.remove(requestCode)
    }
}