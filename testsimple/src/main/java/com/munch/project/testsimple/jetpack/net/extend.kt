package com.munch.project.testsimple.jetpack.net

import com.munch.project.testsimple.jetpack.model.BaseDtoWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Create by munch1182 on 2020/12/19 13:44.
 */

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
