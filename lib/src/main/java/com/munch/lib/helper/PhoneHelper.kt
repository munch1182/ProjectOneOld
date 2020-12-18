package com.munch.lib.helper

import android.os.Build

/**
 * Create by munch1182 on 2020/12/18 10:25.
 */
object PhoneHelper {

    fun getBrand(): String? = Build.BRAND

    fun getVersion() = Build.VERSION.SDK_INT
}