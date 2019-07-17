package com.munhc.lib.libnative.helper

import android.view.View
import android.view.ViewGroup

/**
 * Created by Munch on 2019/7/15 13:50
 */
object ViewHelper {

    fun getParams(view: View): ViewGroup.LayoutParams {
        var parameter: ViewGroup.LayoutParams? = view.layoutParams
        if (parameter == null) {
            parameter =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        return parameter
    }
}