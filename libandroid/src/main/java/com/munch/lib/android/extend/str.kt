package com.munch.lib.android.extend

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.munch.lib.android.AppHelper

interface SealedClassToString {
    override fun toString(): String
}

open class SealedClassToStringByName : SealedClassToString {
    override fun toString(): String {
        return javaClass.simpleName
    }
}

/**
 * 判断权限是否被授予
 */
fun String.isGranted() =
    ContextCompat.checkSelfPermission(AppHelper, this) == PackageManager.PERMISSION_GRANTED

/**
 * 判断被拒绝的权限是否是被永久拒绝
 */
fun String.isDenied(act: Activity) = !ActivityCompat.shouldShowRequestPermissionRationale(act, this)