package com.munch.project.testsimple.jetpack.model.dto

/**
 * Create by munch1182 on 2020/12/18 15:41.
 */
data class BaseDtoWrapper<T>(
    val data: T? = null, val errorCode: Int = 0,
    val errorMsg: String = ""
) {

    fun noError(): Boolean {
        return errorCode == 0 /*&& data != null*/
    }
}