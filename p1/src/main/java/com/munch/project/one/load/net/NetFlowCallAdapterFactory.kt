package com.munch.project.one.load.net

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 处理返回值为Flow的Retrofit请求
 *
 * Create by munch1182 on 2021/9/16 17:31.
 */
class NetFlowCallAdapterFactory : CallAdapter.Factory() {

    companion object {

        fun create() = NetFlowCallAdapterFactory()
    }

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        //如果返回不是Flow则不处理
        if (Flow::class.java != getRawType(returnType)) {
            return null
        }

        if (returnType !is ParameterizedType) {
            return null
        }
        //获取Flow泛型的Type
        val responseType = getParameterUpperBound(0, returnType)
        return BodyCallAdapter<Any>(responseType)
    }

    private class BodyCallAdapter<R>(
        private val responseType: Type
    ) : CallAdapter<R, Flow<R>> {

        override fun adapt(call: Call<R>) = flow<R> {
            emit(
                suspendCancellableCoroutine { continuation ->
                    call.enqueue(object : Callback<R> {
                        override fun onFailure(call: Call<R>, t: Throwable) {
                            if (continuation.isCancelled) {
                                return
                            }
                            continuation.resumeWithException(t)
                        }


                        override fun onResponse(call: Call<R>, response: Response<R>) {
                            if (response.isSuccessful) {
                                response.body()?.let {
                                    try {
                                        continuation.resume(it)
                                    } catch (ex: Exception) {
                                        continuation.resumeWithException(ex)
                                    }
                                }
                                    ?: continuation.resumeWithException(NullPointerException("ResponseBody is null:$response"))
                            } else {
                                continuation.resumeWithException(HttpException(response))
                            }
                        }
                    })
                    continuation.invokeOnCancellation {
                        try {
                            call.cancel()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        }

        override fun responseType() = responseType
    }
}