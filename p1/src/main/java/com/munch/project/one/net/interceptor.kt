package com.munch.project.one.net

import androidx.collection.arrayMapOf
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * 使用
 * @Headers("${HEADER_BASE_URL}: url1")
 * 或
 * BASE_URL_1 = (HEADER_BASE_URL+ type1) + @Headers("BASE_URL_1") + register("BASE_URL_1", "https://url1".toHttpUrlOrNull)
 * 来声明需要替换BaseUrl
 */
object NetBaseUrlReplaceInterceptor : Interceptor {

    const val HEADER_BASE_URL = "x-base-url"

    private val urls = arrayMapOf<String, HttpUrl>()

    fun register(header: String, url: HttpUrl): NetBaseUrlReplaceInterceptor {
        urls[header] = url
        return this
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originReq = chain.request()
        var req = originReq

        val urlHeaders = originReq.headers(HEADER_BASE_URL)
        val header = urlHeaders.find { it.contains(HEADER_BASE_URL) }

        if (header != null) {
            val newBaseUrl = getUrl(header)
            if (newBaseUrl != null) {
                val newBuilder = originReq.newBuilder()
                newBuilder.removeHeader(header)
                val newUrl = originReq.url.newBuilder()
                    .scheme(newBaseUrl.scheme)
                    .host(newBaseUrl.host)
                    .port(newBaseUrl.port)
                    .build()
                newBuilder.url(newUrl)
                req = newBuilder.build()
            }
        }
        return chain.proceed(req)
    }

    /**
     * 从header中回去BaseUrl
     *
     * 如果已经注册[register], 会调用注册的url
     * 如果[header]的格式为[header:url], 则会使用url
     * 否则, 会返回null
     */
    private fun getUrl(header: String): HttpUrl? {
        var url = urls[header]
        if (url == null) {
            val split = header.split(":")
            if (split.size > 1) {
                url = split[1].trim().toHttpUrlOrNull()
            }
        }
        return url
    }
}

/**
 * 添加公用header
 */
object CommonHeaderInterceptor : Interceptor {

    private val header = arrayMapOf<String, () -> String?>()

    fun add(key: String, value: () -> String?): CommonHeaderInterceptor {
        header[key] = value
        return this
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (header.isEmpty) {
            return chain.proceed(chain.request())
        }
        val req = chain.request().newBuilder().apply {
            header.forEach { (t, u) -> addHeader(t, u?.invoke() ?: "") }
        }.build()
        return chain.proceed(req)
    }
}

/**
 * 当网络请求抛出异常时, 拦截异常并返回自行构建的json
 * 主要可以用json的code统一网络错误和请求错误
 *
 * 注意加入拦截器时的顺序
 */
object NetErrorHandleInterceptor : Interceptor {

    private var errorJson: String? = null
    private var errorHandle: ((e: Exception, chain: Interceptor.Chain) -> String)? = null

    fun setErrorJson(json: String): NetErrorHandleInterceptor {
        errorJson = json
        return this
    }

    fun setErrorHandle(errorHandle: ((e: Exception, chain: Interceptor.Chain) -> String)): NetErrorHandleInterceptor {
        NetErrorHandleInterceptor.errorHandle = errorHandle
        return this
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originReq = chain.request()
        try {
            val res = chain.proceed(originReq)
            if (!res.isSuccessful) {
                throw IllegalStateException(res.message)
            }
            return res
        } catch (e: Exception) {
            // 优先级
            val json = errorHandle?.invoke(e, chain) ?: errorJson ?: "{}"
            return Response.Builder()
                .code(200)
                .request(originReq)
                .protocol(Protocol.HTTP_1_1)
                .message("error: ${e.message}")
                .body(json.toResponseBody("application/json".toMediaType()))
                .build()
        }

    }
}

/**
 * 在请求前判断header, 并可以进行更改(可以进行前置请求)
 **/
object HeaderCheckInterceptor : Interceptor {

    private var complete: ((HttpUrl, Headers) -> Map<String, String>?)? = null

    fun setCompleteHeader(complete: (HttpUrl, Headers) -> Map<String, String>?): HeaderCheckInterceptor {
        HeaderCheckInterceptor.complete = complete
        return this
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originReq = chain.request()
        if (complete != null) {
            val headers = originReq.headers
            val h = complete!!.invoke(originReq.url, headers)
            if (h != null) {
                val b = originReq.newBuilder().apply {
                    h.forEach { (t, u) ->
                        removeHeader(t)
                        addHeader(t, u)
                    }
                }.build()
                return chain.proceed(b)
            }
        }
        return chain.proceed(originReq)
    }
}