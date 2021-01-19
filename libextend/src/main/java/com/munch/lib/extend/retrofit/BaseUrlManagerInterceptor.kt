package com.munch.lib.extend.retrofit

import com.munch.lib.extend.retrofit.BaseUrlManager.Companion.getInstance
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 通过拦截器的方式更改retrofit的BaseUrl
 *
 * 调用: [BaseUrlManager.setBaseUrl]
 *
 * Create by munch1182 on 2021/1/19 13:57.
 */
class BaseUrlManagerInterceptor : Interceptor {

    private val manager = getInstance()

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val oldUrl = oldRequest.url
        val baseUrl = oldUrl.redact().replace("...", "")
        return if (manager.baseUrl.isEmpty() || manager.baseUrl == baseUrl) {
            manager.baseUrl = baseUrl
            chain.proceed(oldRequest)
            //更换BaseUrl
        } else {
            val newUrl = oldUrl.toString().replace(baseUrl, manager.baseUrl)
            val newRequest = oldRequest.newBuilder().url(newUrl).build()
            chain.proceed(newRequest)
        }
    }
}

/**
 * 可以直接替换成object，但还是[getInstance]更能体现单例
 */
class BaseUrlManager private constructor() {

    companion object {

        private val INSTANCE by lazy { BaseUrlManager() }

        fun getInstance(): BaseUrlManager = INSTANCE
    }

    internal var baseUrl = ""

    /**
     * 全局更换BaseUrl，到下一次更换为止
     */
    fun setBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl.run {
            if (endsWith("/")) {
                return@run this
            } else {
                return@run this.plus("/")
            }
        }
    }
}