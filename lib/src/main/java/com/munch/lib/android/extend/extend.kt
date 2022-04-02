@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

import android.os.Process
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass
import kotlin.system.exitProcess

/**
 * Create by munch1182 on 2021/8/19 15:05.
 */

inline fun <T> MutableLiveData<T>.toImmutable(): LiveData<T> = this

inline fun <T> MutableStateFlow<T>.toImmutable(): StateFlow<T> = this

fun destroy() {
    Process.killProcess(Process.myPid())
    exitProcess(1)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> KClass<T>.toClass(): Class<T>? = try {
    Class.forName(qualifiedName!!) as Class<T>
} catch (e: Exception) {
    e.printStackTrace()
    null
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> KClass<T>.newInstance(): T? = try {
    Class.forName(qualifiedName!!).newInstance() as T
} catch (e: Exception) {
    e.printStackTrace()
    null
}

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