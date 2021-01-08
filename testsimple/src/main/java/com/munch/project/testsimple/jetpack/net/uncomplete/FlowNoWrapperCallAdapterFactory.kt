package com.munch.project.testsimple.jetpack.net.uncomplete

import com.munch.lib.UNCOMPLETE
import com.munch.lib.log
import com.munch.project.testsimple.jetpack.model.dto.BaseDtoWrapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Create by munch1182 on 2020/12/17 21:55.
 */
@UNCOMPLETE
class FlowNoWrapperCallAdapterFactory : CallAdapter.Factory() {

    companion object {

        @JvmStatic
        fun create() = FlowNoWrapperCallAdapterFactory()
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
        //获取Flow泛型的Type
        val responseType = getParameterUpperBound(0, returnType)
        //获取Flow泛型的class
        val rawFlowType = getRawType(responseType)
        log(responseType, rawFlowType)
        //不处理结果带BaseWrapperDto的请求
        if (responseType == BaseDtoWrapper::class.java) {
            return null
        }
        return BodyCallAdapter<Any>()
    }

    /**
     * 未解决的问题，因为没有理解Converter与CallAdapter之间的关系，以及泛型擦除之类的
     */
    private class BodyCallAdapter<R> : CallAdapter<BaseDtoWrapper<R>, FlowNoWrapper<R>> {

        @ExperimentalCoroutinesApi
        override fun adapt(call: Call<BaseDtoWrapper<R>>) = FlowNoWrapper<R> {
            emit(
                suspendCancellableCoroutine { continuation ->
                    call.enqueue(object : Callback<BaseDtoWrapper<R>> {
                        override fun onFailure(call: Call<BaseDtoWrapper<R>>, t: Throwable) {
                            if (continuation.isCancelled) {
                                return
                            }
                            continuation.resumeWithException(t)
                        }

                        override fun onResponse(
                            call: Call<BaseDtoWrapper<R>>,
                            response: Response<BaseDtoWrapper<R>>
                        ) {
                            if (response.isSuccessful) {
                                response.body()?.let {
                                    when {
                                        it.errorCode != 0 -> {
                                            continuation.resumeWithException(Exception("errorCode:${it.errorCode},errorMsg:${it.errorMsg}"))
                                        }
                                        it.data == null -> {
                                            continuation.resumeWithException(
                                                NullPointerException("data is null")
                                            )
                                        }
                                        else -> {
                                            try {
                                                continuation.resume(it.data)
                                            } catch (ex: Exception) {
                                                ex.printStackTrace()
                                                continuation.resumeWithException(ex)
                                            }
                                        }
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

        override fun responseType() = FlowNoWrapper::class.java
    }
}