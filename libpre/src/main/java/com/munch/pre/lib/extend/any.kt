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


/**
 * 默认一个参数的单例
 * 使用：私有构造后使用 companion object : SingletonHolder<T, A>(::T)或者companion object : SingletonHolder<T, A>({creator}})
 * 无参数的可以直接使用lazy
 * @see kotlin.SynchronizedLazyImpl
 */
open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {

    private var creator: ((A) -> T)? = creator

    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }
        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }

}

open class SingletonHolder2<out T : Any, in A, in B>(creator: (A, B) -> T) {

    private var creator: ((A, B) -> T)? = creator

    @Volatile
    private var instance: T? = null

    fun getInstance(argA: A, argB: B): T {
        val i = instance
        if (i != null) {
            return i
        }
        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(argA, argB)
                instance = created
                creator = null
                created
            }
        }
    }

}