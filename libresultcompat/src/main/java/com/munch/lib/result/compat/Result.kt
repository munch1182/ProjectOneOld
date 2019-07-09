package com.munch.lib.result.compat

import android.content.Intent
import android.support.v4.app.Fragment

/**
 * Created by Munch on 2019/7/9 11:08
 */
class Result {

    internal var fragment: Fragment? = null
    internal var intent: Intent? = null
    internal var requestCode: Int? = null
    internal var listener: ResultListener? = null

    fun result(listener: ResultListener) {
        this.listener = listener
        request()
    }

    private fun request() {
        if (fragment != null && intent != null && this.listener != null) {
            fragment?.startActivityForResult(intent, requestCode!!)
        }
    }
}