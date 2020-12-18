package com.munch.project.testsimple.jetpack

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
@UNCOMPLETE
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
        if (Flow::class.java != getRawType(returnType)) {
            return null
        }

        if (returnType !is ParameterizedType) {
            throw IllegalStateException("Flow return type must be parameterized as Flow<Foo> or Flow<out Foo>")
        }

        val responseType = getParameterUpperBound(0, returnType)

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

    private class ResponseCallAdapter<T>(
        private val responseType: Type
    ) : CallAdapter<T, Flow<Response<T>>> {

        @ExperimentalCoroutinesApi
        override fun adapt(call: Call<T>): Flow<Response<T>> {
            return flow {
                // TODO: 2020/12/18 遇到编译问题，暂时不管
                /*emit(
                    suspendCancellableCoroutine { continuation ->
                        call.enqueue(object : Callback<T> {
                            override fun onFailure(call: Call<T>, t: Throwable) =
                                continuation.resumeWithException(t)

                            override fun onResponse(call: Call<T>, response: Response<T>) =
                                continuation.resume(response, null)
                        })
                        continuation.invokeOnCancellation { call.cancel() }
                    }

                )*/
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
                        override fun onFailure(call: Call<R>, t: Throwable) =
                            continuation.resumeWithException(t)

                        override fun onResponse(call: Call<R>, response: Response<R>) = try {
                            continuation.resume(response.body()!!, null)
                        } catch (ex: Exception) {
                            continuation.resumeWithException(ex)
                        }

                    })
                    continuation.invokeOnCancellation { call.cancel() }
                }
            )
        }

        override fun responseType() = responseType
    }
}