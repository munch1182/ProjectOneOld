package com.munch.lib.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Rect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.munch.lib.log.log

/**
 * Create by munch1182 on 2021/8/19 15:05.
 */

fun <T> MutableLiveData<T>.toLive(): LiveData<T> = this

@SuppressLint("QueryPermissionsNeeded")
fun Context.isRegistered(action: String) {
    packageManager.queryBroadcastReceivers(
        Intent().apply { setAction(action) }, 0
    ).forEach {
        log(it)
    }
}

fun Paint.measureTextBounds(text: String, bound: Rect = Rect()): Rect {
    getTextBounds(text, 0, text.length, bound)
    return bound
}
