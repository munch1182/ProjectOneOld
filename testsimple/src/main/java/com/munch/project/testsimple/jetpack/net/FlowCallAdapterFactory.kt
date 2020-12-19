package com.munch.project.testsimple.jetpack.net

import com.munch.lib.UNCOMPLETE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.coroutines.resumeWithException

/**
 * Create by munch1182 on 2020/12/17 21:55.
 */
class FlowCallAdapterFactory : CallAdapter.Factory() {

    companion object {

        @JvmStatic
        fun create() = FlowCallAdapterFactory()
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
            throw IllegalStateException("Flow return type must be parameterized as Flow<Foo> or Flow<out Foo>")
        }
        //获取Flow泛型的Type
        val responseType = getParameterUpperBound(0, returnType)
        //获取Flow泛型的class
        val rawFlowType = getRawType(responseType)

        return if (rawFlowType == Response::class.java) {
            if (responseType !is ParameterizedType) {
                throw IllegalStateException(
                    "Response must be parameterized as Response<Foo> or Response<out Foo>"
                )
            }

            ResponseCallAdapter<Any>(getParameterUpperBound(0, responseType))
        } else {
            BodyCallAdapter<Any>(responseType)
        }
    }

    @UNCOMPLETE
    private class ResponseCallAdapter<T>(
        private val responseType: Type
    ) : CallAdapter<T, Flow<Response<T>>> {

        @ExperimentalCoroutinesApi
        override fun adapt(call: Call<T>): Flow<Response<T>> {
            return flow {
                /*emit(
                    suspendCancellableCoroutine { continuation ->
                        call.enqueue(object : Callback<T> {
                            override fun onFailure(call: Call<T>, t: Throwable) =
                                continuation.resumeWithException(t)

                            override fun onResponse(call: Call<T>, response: Response<T>) =
                                continuation.resume(response)
                        })
                        continuation.invokeOnCancellation { call.cancel() }
                    }
                )*/
                throw Exception("未完成")
            }
        }

        override fun responseType() = responseType
    }

    private class BodyCallAdapter<R>(
        private val responseType: Type
    ) : CallAdapter<R, Flow<R>> {

        @ExperimentalCoroutinesApi
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
                                        continuation.resume(it, null)
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