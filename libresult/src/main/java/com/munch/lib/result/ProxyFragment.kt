package com.munch.lib.result

import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Created by Munch on 2019/7/9 10:53
 */
class ProxyFragment : Fragment() {

    private var result: Result? = null
    private var permission: Permission? = null

    fun start4Result(intent: Intent, requestCode: Int): Result {
        result = Result()
        result!!.requestCode = requestCode
        result!!.intent = intent
        result!!.fragment = this
        return result!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == result?.requestCode) {
            result?.listener?.result(resultCode, data ?: Intent())
            result = null
        }
    }

    fun requestPermissions(requestCode: Int, permissions: Array<out String>): Permission {
        permission = Permission()
        permission!!.requestCode = requestCode
        permission!!.permissions = permissions
        permission!!.fragment = this
        return permission!!
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permission?.requestCode) {
            permission?.onPermissionsResult(permissions, grantResults)
            permission = null
        }
    }
}