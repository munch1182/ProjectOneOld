package com.munch.project.testsimple.jetpack.net

import com.munch.lib.extend.retrofit.ApiResult
import com.munch.project.testsimple.jetpack.model.dto.BaseDtoWrapper
import okhttp3.Request
import okio.Timeout
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 需要先于ApiResultCallAdapterFactory加入retrofit
 *
 * 将返回类型包装为[ApiResult]
 *
 * 相较于[com.munch.lib.extend.retrofit.ApiResultCallAdapterFactory]，
 * 此Factory使用时对于retrofit的申明返回值时只需声明ApiResult<T>，而不是声明
 * ApiResult<BaseDtoWrapper<T>>，返回值也直接返回了ApiResult<T>
 * 对于BaseDtoWrapper的[BaseDtoWrapper.errorCode]和[BaseDtoWrapper.errorMsg]也传进了[ApiResult.Fail]
 *
 * Create by munch1182 on 2021/1/19 9:57.
 */
class ApiResultNoWrapperCallAdapterFactory : CallAdapter.Factory() {

    companion object {

        fun create() = ApiResultNoWrapperCallAdapterFactory()
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

        check(returnType is ParameterizedType) { "$returnType must be parameterized. Raw types are not supported." }

        val apiResultType = getParameterUpperBound(0, returnType)
        //返回值不是ApiResult的不处理
        if (ApiResult::class.java != getRawType(apiResultType)) {
            return null
        }
        check(apiResultType is ParameterizedType) { "$returnType must be parameterized. Raw types are not supported." }
        //<T>的类型
        //retrofit类型转换的关键，此类型是使用retrofit自定义请求的返回值
        //而ApiResultCallAdapter的adapt的返回值则是转换后的返回值
        val responseType = getParameterUpperBound(0, apiResultType)
        //不处理返回类型直接声明带有BaseDtoWrapper的类型，即ApiResult<BaseDtoWrapper<T>>，
        //这个交还ApiResultCallAdapterFactory去处理，所以要先添加
        if (BaseDtoWrapper::class.java == getRawType(responseType)) {
            return null
        }
        return ApiResultNoWrapperCallAdapter<Any>(BaseDtoWrapper::class.java)
    }
}

/**
 * 将call<T>转为Call<ApiResult<T>>
 */
class ApiResultNoWrapperCallAdapter<T>(private val type: Type) :
    CallAdapter<BaseDtoWrapper<T>, Call<ApiResult<T>>> {
    override fun responseType() = type
    override fun adapt(call: Call<BaseDtoWrapper<T>>): Call<ApiResult<T>> {
        return ApiResultNoWrapperCall(call)
    }
}

class ApiResultNoWrapperCall<T>(private val delegate: Call<BaseDtoWrapper<T>>) :
    Call<ApiResult<T>> {

    override fun clone(): Call<ApiResult<T>> {
        return ApiResultNoWrapperCall(delegate.clone())
    }

    override fun execute(): Response<ApiResult<T>> {
        return resWithApi(delegate.execute())
    }

    override fun enqueue(callback: Callback<ApiResult<T>>) {
        delegate.enqueue(object : Callback<BaseDtoWrapper<T>> {
            override fun onResponse(
                call: Call<BaseDtoWrapper<T>>,
                response: Response<BaseDtoWrapper<T>>
            ) {
                callback.onResponse(this@ApiResultNoWrapperCall, resWithApi(response))
            }

            override fun onFailure(call: Call<BaseDtoWrapper<T>>, t: Throwable) {
                callback.onFailure(this@ApiResultNoWrapperCall, t)
            }
        })
    }


    private fun resWithApi(response: Response<BaseDtoWrapper<T>>): Response<ApiResult<T>> {
        return if (response.isSuccessful) {
            val data = response.body()
            val res = if (data == null) {
                ApiResult.Fail.newNullDataFail()
            } else {
                if (data.noError()) {
                    ApiResult.Success(data.data)
                } else {
                    ApiResult.Fail(data.errorCode, data.errorMsg)
                }
            }
            Response.success(res)
        } else {
            Response.success(ApiResult.Fail.newHttpStatusFail("http status fail: ${response.code()}"))
        }
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