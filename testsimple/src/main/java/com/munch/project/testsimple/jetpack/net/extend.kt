package com.munch.project.testsimple.jetpack.net

import com.munch.project.testsimple.jetpack.model.dto.BaseDtoWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Create by munch1182 on 2020/12/19 13:44.
 */
@Throws(Exception::class)
fun <T> Flow<BaseDtoWrapper<T>>.noWrapper(): Flow<T> {
    return this@noWrapper.map {
        if (it.errorCode == 0) {
            if (it.data == null) {
                throw NullPointerException("data is null")
            }
            return@map it.data
        } else {
            throw Exception("errorCode:${it.errorCode},errorMsg:${it.errorMsg}")
        }
    }
}

@Throws(Exception::class)
fun <T> BaseDtoWrapper<T>.noWrapper(): T {
    if (this.errorCode == 0) {
        if (this.data == null) {
            throw NullPointerException("data is null")
        }
        return this.data
    } else {
        throw Exception("errorCode:${this.errorCode},errorMsg:${this.errorMsg}")
    }
}
