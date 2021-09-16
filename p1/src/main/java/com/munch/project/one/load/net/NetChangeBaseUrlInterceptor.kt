package com.munch.project.one.load.net

import com.munch.project.one.load.net.NetChangeBaseUrlInterceptor.Companion.NAME_HEADER
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 通过[NAME_HEADER]的Header来获取通过[header2Url]获取BaseUrl并更改，如果没有[NAME_HEADER]或者[header2Url]返回为null，则不更改
 *
 * 使用:
 * 1.
 *    const val HEADER_OTHER = "${NetChangeBaseUrlInterceptor.NAME_HEADER}:OTHER"
 *
 * 2.
 *     @GET("/xxx")
 *     @Headers(HEADER_OTHER)
 *     fun test(): NetResult<String>
 *
 * 3.
 * OkHttpClient.Builder()
 *  .callTimeout(10L, TimeUnit.MILLISECONDS)
 *  .readTimeout(10L, TimeUnit.MILLISECONDS)
 *  .addInterceptor(NetChangeBaseUrlInterceptor {
 *       if (it == HEADER_OTHER) "https://xxx" else null
 *  })
 *
 * Create by munch1182 on 2021/9/16 16:38.
 */
class NetChangeBaseUrlInterceptor(private val header2Url: (header: String) -> String?) :
    Interceptor {

    companion object {
        const val NAME_HEADER = "url_name"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val newBuilder = originRequest.newBuilder()
        val headers = originRequest.headers(NAME_HEADER)
        if (headers.isNotEmpty()) {
            //移除该标志header
            newBuilder.removeHeader(NAME_HEADER)
            val newUrl = header2Url.invoke(headers[0])?.toHttpUrlOrNull() ?: originRequest.url
            newBuilder.url(
                originRequest.url.newBuilder()
                    .scheme(newUrl.scheme)
                    .host(newUrl.host)
                    .port(newUrl.port)
                    .build()
            )
        }
        return chain.proceed(newBuilder.build())
    }
}