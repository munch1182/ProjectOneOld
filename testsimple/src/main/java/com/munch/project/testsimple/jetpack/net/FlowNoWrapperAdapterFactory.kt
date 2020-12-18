package com.munch.project.testsimple.jetpack.net

import com.munch.lib.UNCOMPLETE
import com.munch.lib.log
import com.munch.project.testsimple.jetpack.model.BaseWrapperDto
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
class FlowNoWrapperAdapterFactory : CallAdapter.Factory() {

    companion object {

        @JvmStatic
        fun create() = FlowNoWrapperAdapterFactory()
    }

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (FlowNoWrapper::class.java != getRawType(returnType)) {
            return null
        }

        if (returnType !is ParameterizedType) {
            throw IllegalStateException("Flow return type must be parameterized as Flow<Foo> or Flow<out Foo>")
        }

        val responseType = getParameterUpperBound(0, returnType)
        log(responseType)

        return BodyCallAdapter<Any>(BaseWrapperDto::class.java)
    }

    private class BodyCallAdapter<T>(
        private val responseType: Type
    ) : CallAdapter<BaseWrapperDto<*>, Flow<T>> {

        @ExperimentalCoroutinesApi
        override fun adapt(call: Call<BaseWrapperDto<*>>): Flow<T> {
            return flow {
                emit(suspendCancellableCoroutine { continuation ->
                    call.enqueue(object : Callback<BaseWrapperDto<*>> {
                        override fun onResponse(
                            call: Call<BaseWrapperDto<*>>,
                            response: Response<BaseWrapperDto<*>>
                        ) {
                            val body = response.body()
                            if (body?.errorCode != 0) {
                                continuation.resume(body?.data, null)
                            } else {
                                continuation.resumeWithException(Exception("errorCode:${body.errorCode},errorMsg:${body.errorMsg}"))
                            }
                        }

                        override fun onFailure(call: Call<BaseWrapperDto<*>>, t: Throwable) {
                            continuation.resumeWithException(t)
                        }
                    })
                })
            }
        }

        override fun responseType() = responseType
    }
}