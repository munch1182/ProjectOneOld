package com.munch.lib.base

import android.graphics.Paint
import android.graphics.Rect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.munch.lib.app.AppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass

/**
 * Create by munch1182 on 2021/8/19 15:05.
 */

fun <T> MutableLiveData<T>.toImmutable(): LiveData<T> = this

fun <T> MutableStateFlow<T>.toImmutable(): StateFlow<T> = this

fun putStr2Clip(content: String) = AppHelper.app.putStr2Clip(content)

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

fun Paint.measureTextBounds(text: String, bound: Rect = Rect()): Rect {
    getTextBounds(text, 0, text.length, bound)
    return bound
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
