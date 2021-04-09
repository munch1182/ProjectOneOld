package com.munch.pre.lib.extend

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.munch.pre.lib.log.LogLog

/**
 * Create by munch1182 on 2021/3/31 13:40.
 */
fun log(vararg any: Any?) {
    val a = when {
        any.isEmpty() -> ""
        any.size == 1 -> any[0]
        else -> any
    }
    LogLog.methodOffset(1).log(a)
}

fun logJson(json: String) {
    LogLog.methodOffset(1).isJson().log(json)
}

@MainThread
fun <T> LiveData<T>.observeOnChanged(
    owner: LifecycleOwner,
    onChanged: (T) -> Unit
) {
    observe(owner, Observer(onChanged))
}

fun <T> MutableLiveData<T>.toLiveData(): LiveData<T> = this