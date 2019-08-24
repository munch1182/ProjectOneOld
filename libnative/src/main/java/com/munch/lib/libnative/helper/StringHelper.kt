package com.munch.lib.libnative.helper

import android.text.TextUtils

/**
 * Created by Munch on 2019/8/20 15:12
 */
object StringHelper {

    @JvmStatic
    fun isEmpty(str: CharSequence?) = TextUtils.isEmpty(str)
}