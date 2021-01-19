package com.munch.lib.extend.retrofit

import com.munch.lib.BaseApp

/**
 * 涵盖了网络状态错误的返回包装类
 *
 * Create by munch1182 on 2021/1/19 9:57.
 */
sealed class ApiResult<out T> {

    data class Success<out T>(val data: T?) : ApiResult<T>()

    data class Fail(val code: Int, val msg: String) : ApiResult<Nothing>() {

        fun isNetError() = code == CODE_HTTP_STATUS

        /**
         * 该异常是否能被忽略
         *
         * 理论上来说正式版才能使用
         */
        fun canIgnore(): Boolean {
            return !BaseApp.debugMode() && (code == CODE_DATA_IS_NULL || code == CODE_HTTP_STATUS)
        }

        companion object {

            private const val CODE_HTTP_STATUS = -11
            private const val CODE_DATA_IS_NULL = -12

            fun newHttpStatusFail(msg: String = "http status fail") = Fail(CODE_HTTP_STATUS, msg)

            fun newNullDataFail(msg: String = "data is null") = Fail(CODE_DATA_IS_NULL, msg)
        }
    }
}