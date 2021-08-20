package com.munch.lib.result

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.SparseArray
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
    private val permissionMap: SparseArray<ResultHelper.OnPermissionCallback> = SparseArray(2)
    private val resultMap: SparseArray<ResultHelper.OnResultCallback> = SparseArray(2)

    private fun newRequestCode() = requestCode.incrementAndGet()

    fun startActivityForResult(
        intent: Intent?,
        callback: ResultHelper.OnResultCallback
    ) {
        val requestCode = newRequestCode()
        resultMap.put(requestCode, callback)
        @Suppress("DEPRECATION")
        super.startActivityForResult(intent, requestCode)
    }

    fun requestPermissions(
        permissions: Array<out String>,
        callback: ResultHelper.OnPermissionCallback
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