@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 获取当前线程名
 */
inline val threadName: String
    get() = Thread.currentThread().name

/**
 * 获取当前线程ID
 */
inline val threadId: Long
    get() = Thread.currentThread().id

/**
 * 寻找[Type]上继承自[target]泛型的类
 *
 * 如果找不到, 则返回null
 */
@Suppress("UNCHECKED_CAST")
fun <T> Type.findParameterized(target: Class<in T>): Class<T>? {
    when (this) {
        is ParameterizedType -> {
            val c = actualTypeArguments.find { it is Class<*> && target.isAssignableFrom(it) }
            if (c != null) return c as Class<T>
            val type = this.rawType
            if (type == Any::class.java) return null
            return type.findParameterized(target)
        }
        is Class<*> -> {
            return if (this == Any::class.java) null
            else this.genericSuperclass?.findParameterized(target)
        }
        else -> return null
    }
}

inline fun <T> lazy(noinline initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun <T> lazySync(noinline initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.SYNCHRONIZED, initializer)