package com.munch.lib.extend.retrofit

import okhttp3.Request
import okio.Timeout
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 将返回类型包装为[ApiResult]，支持[suspend]请求返回ApiResult<T>，否则应该返回Call<ApiResult<T>>
 *
 * Create by munch1182 on 2021/1/19 11:36.
 */
class ApiResultCallAdapterFactory : CallAdapter.Factory() {

    companion object {

        fun create() = ApiResultCallAdapterFactory()
    }

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        //对于被suspend标记返回ApiResult<T>的retrofit请求，返回值实际是call<ApiResult<T>>
        if (Call::class.java != getRawType(returnType)) {
            return null
        }

        check(returnType is ParameterizedType) { "$returnType must be parameterized. Raw types are not supported" }

        val apiResultType = getParameterUpperBound(0, returnType)
        //返回值不是ApiResult的不处理
        if (ApiResult::class.java != getRawType(apiResultType)) {
            return null
        }
        check(apiResultType is ParameterizedType) { "$returnType must be parameterized. Raw types are not supported" }
        //<T>的类型
        //retrofit类型转换的关键，此类型是使用retrofit自定义请求的返回值
        //而ApiResultCallAdapter的adapt的返回值则是转换后的返回值
        val responseType = getParameterUpperBound(0, apiResultType)
        return ApiResultCallAdapter<Any>(responseType)
    }
}

/**
 * 将call<T>转为Call<ApiResult<T>>
 */
class ApiResultCallAdapter<T>(private val type: Type) : CallAdapter<T, Call<ApiResult<T>>> {
    override fun responseType() = type
    override fun adapt(call: Call<T>): Call<ApiResult<T>> {
        return ApiResultCall(call)
    }
}

class ApiResultCall<T>(private val delegate: Call<T>) : Call<ApiResult<T>> {
    override fun clone(): Call<ApiResult<T>> {
        return ApiResultCall(delegate.clone())
    }

    override fun execute(): Response<ApiResult<T>> {
        return responseWithApiResult(delegate.execute())
    }

    private fun responseWithApiResult(response: Response<T>): Response<ApiResult<T>> {
        return if (response.isSuccessful) {
            val res = if (response.body() == null) {
                ApiResult.Fail.newNullDataFail()
            } else {
                ApiResult.Success(response.body())
            }
            Response.success(res)
        } else {
            Response.success(ApiResult.Fail.newHttpStatusFail("http status fail: ${response.code()}"))
        }
    }

    override fun enqueue(callback: Callback<ApiResult<T>>) {
        delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                callback.onResponse(this@ApiResultCall, responseWithApiResult(response))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onFailure(this@ApiResultCall, t)
            }
        })
    }

    override fun isExecuted() = delegate.isExecuted

    override fun cancel() {
        delegate.cancel()
    }

    override fun isCanceled() = delegate.isCanceled

    override fun request(): Request {
        return delegate.request()
    }

    override fun timeout(): Timeout {
        return delegate.timeout()
    }
}