package com.munch.project.one.net

import com.munch.lib.android.extend.SealedClassToStringByName
import com.munch.lib.android.log.LogInfo
import com.munch.lib.android.log.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

/**
 * Create by munch1182 on 2022/9/22 17:30.
 */
object NetHelper {

    const val HEADER = "baseUrl"
    private val log = Logger("net", LogInfo.None)
    private val multiBaseUrlHelper = MultiBaseUrlInterceptor(HEADER) // 通过header切换baseUrl

    val client = OkHttpClient
        .Builder()
        .addInterceptor(multiBaseUrlHelper)
        .addInterceptor(
            HttpLoggingInterceptor { log.log(it) }.apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
        )
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://www")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val img = retrofit.create<BiYing>()
}


interface NetResultBean<T> {
    val result: NetResult
    val data: T?
}

sealed class NetResult : SealedClassToStringByName() {
    object Success : NetResult()
    class Fail(val reason: String?, val exception: Exception? = null) : NetResult()

    val isSuccess: Boolean
        get() = this is Success
}