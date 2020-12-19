package com.munch.project.testsimple.jetpack.model

/**
 * Create by munch1182 on 2020/12/18 15:41.
 */
data class BaseDtoWrapper<T>(
    val data: T? = null, val errorCode: Int = 0,
    val errorMsg: String = ""
)