package com.munch.lib.result

import android.util.Log
import androidx.fragment.app.Fragment

/**
 * Created by Munch on 2019/7/9 13:52
 */
class Permission {

    internal var fragment: Fragment? = null
    internal var permissions: Array<out String>? = null
    internal var requestCode: Int? = null
    internal var listener: PermissionListener? = null

    fun result(listener: PermissionListener) {
        this.listener = listener
        request()
    }

    private fun request() {
        if (fragment != null && permissions != null && requestCode != null) {
            Log.d("LOGLOG", permissions!!.get(0))
            fragment!!.requestPermissions(permissions!!, requestCode!!)
        }
    }
}