package com.munch.project.one.net

import com.munch.lib.android.extend.catch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 通过声明固定格式的header, 当读取到该Header时, 此Interceptor会将对应的值解析为Url并进行替换
 *
 * Create by munch1182 on 2022/9/23 11:41.
 */
class MultiBaseUrlInterceptor(private val header: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val headers = req.headers(header) // 读取headers中是否有[header]
        if (headers.isNotEmpty()) {
            val newBuilder = chain.request().newBuilder()
            newBuilder.removeHeader(header) // 移除掉这个header
            val newUrl = catch { headers[0].trim().toHttpUrlOrNull() } // 并获取这个值
            if (newUrl != null) {
                val newUrl2 = req.url.newBuilder() // 通过这个url构建新的url
                    .scheme(newUrl.scheme)
                    .host(newUrl.host)
                    .port(newUrl.port)
                    .build()
                return chain.proceed(newBuilder.url(newUrl2).build()) // 并使用新的url进行请求
            }
        }
        return chain.proceed(req)
    }

}