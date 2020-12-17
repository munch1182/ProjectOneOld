package com.munch.lib

import com.munch.lib.helper.LogLog

/**
 * Create by munch1182 on 2020/12/13 16:02.
 */
fun Any.log(vararg any: Any?) {
    val clazz = this::class.java
    when {
        any.isEmpty() -> {
            LogLog.callClass(clazz).log(null)
        }
        any.size == 1 -> {
            LogLog.callClass(clazz).log(any[0])
        }
        else -> {
            LogLog.callClass(clazz).log(any)
        }
    }
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