//package com.munch.project.testsimple.jetpack
//
//import com.munch.lib.UNCOMPLETE
//import com.munch.lib.UNTEST
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.suspendCancellableCoroutine
//import retrofit2.*
//import java.lang.reflect.ParameterizedType
//import java.lang.reflect.Type
//import kotlin.coroutines.resumeWithException
//
///**
// * Create by munch1182 on 2020/12/17 21:55.
// */
//@UNCOMPLETE
//@UNTEST
//class FlowCallAdapterFactory : CallAdapter.Factory() {
//
//    override fun get(
//        returnType: Type,
//        annotations: Array<Annotation>,
//        retrofit: Retrofit
//    ): CallAdapter<*, *>? {
//        if (Flow::class != getRawType(returnType)) {
//            return null
//        }
//
//
//        if (returnType !is ParameterizedType) {
//            throw IllegalStateException("Flow return type must be parameterized as Flow<Foo> or Flow<out Foo>")
//        }
//
//        val responseType = getParameterUpperBound(0, returnType)
//
//        val rawFlowType = getRawType(responseType)
//
//        return if (rawFlowType == Response::class.java) {
//            if (responseType !is ParameterizedType) {
//                throw IllegalStateException(
//                    "Response must be parameterized as Response<Foo> or Response<out Foo>"
//                )
//            }
//
//            ResponseCallAdapter<Any>(getParameterUpperBound(0, responseType))
//        } else {
//            BodyCallAdapter<Any>(responseType)
//        }
//    }
//
//    private class ResponseCallAdapter<T>(
//        private val responseType: Type
//    ) : CallAdapter<T, Flow<Response<T>>> {
//        @ExperimentalCoroutinesApi
//        override fun adapt(call: Call<T>): Flow<Response<T>> {
//            return flow {
//                emit(
//                    suspendCancellableCoroutine { continuation ->
//                        class RetrofitCallback : Callback<T> {
//                            override fun onFailure(call: Call<T>, t: Throwable) =
//                                continuation.resumeWithException(t)
//
//                            override fun onResponse(call: Call<T>, response: Response<T>) =
//                                continuation.resume(response, null)
//                        }
//                        call.enqueue(RetrofitCallback())
//                        continuation.invokeOnCancellation { call.cancel() }
//                    }
//
//                )
//            }
//        }
//
//        override fun responseType() = responseType
//    }
//
//    private class BodyCallAdapter<T>(
//        private val responseType: Type
//    ) : CallAdapter<T, Flow<T>> {
//
//        @ExperimentalCoroutinesApi
//        override fun adapt(call: Call<T>) = flow<T> {
//            emit(
//                suspendCancellableCoroutine { continuation ->
//                    class RetrofitCallback : Callback<T> {
//                        override fun onFailure(call: Call<T>, t: Throwable) =
//                            continuation.resumeWithException(t)
//
//                        override fun onResponse(call: Call<T>, response: Response<T>) = try {
//                            continuation.resume(response.body()!!, null)
//                        } catch (ex: Exception) {
//                            continuation.resumeWithException(ex)
//                        }
//
//                    }
//                    call.enqueue(RetrofitCallback())
//                    continuation.invokeOnCancellation { call.cancel() }
//                }
//            )
//        }
//
//        override fun responseType() = responseType
//    }
//}