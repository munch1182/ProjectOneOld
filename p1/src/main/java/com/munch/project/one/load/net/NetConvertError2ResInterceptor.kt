package com.munch.project.one.load.net

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * 将所有导致网络请求错误(而不是接口错误)的情况转为接口错误
 *
 * 网络错误：指由网络权限、接口地址、服务器状态等引起的请求错误，如果发生，Retrofit会抛出异常
 * 接口错误：指接口返回该请求失败的情形
 *
 * 将网络错误转为接口错误，即无需每个请求都加入try/catch，
 * 而且可以根据错误标志(一般是ErrorCode)统一处理(根据Interceptor拦截)，也可以各自单独处理(根据返回errorJson)
 *
 * @param errorJson 返回一段自定义的json返回，当网络请求错误发生时，不会抛出异常，而是返回此json，因此此json应该不要与后台返回混淆
 *
 * Create by munch1182 on 2021/9/16 16:38.
 */
class NetErrorConvertResInterceptor(private val errorJson: String) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            val proceed = chain.proceed(request.newBuilder().build())
            if (proceed.isSuccessful) proceed else resWhenNetError(request, proceed)
        } catch (e: Exception) {
            resWhenNetError(request, null)
        }
    }

    private fun resWhenNetError(request: Request, proceed: Response?) =
        Response.Builder()
            .code(200)
            .request(request)
            .protocol(proceed?.protocol ?: Protocol.HTTP_1_1)
            .message(proceed?.message ?: "net error convert res")
            .body(errorJson.toResponseBody("application/json".toMediaType()))
            .build()
}