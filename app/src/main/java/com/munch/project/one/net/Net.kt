package com.munch.project.one.net

import com.munch.lib.android.log.LogInfo
import com.munch.lib.android.log.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Create by munch1182 on 2022/9/22 17:30.
 */
object NetHelper {

    private val log = Logger("net", LogInfo.None)

    val client = OkHttpClient
        .Builder()
        .addInterceptor(HttpLoggingInterceptor { log.log(it) }.apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        })
        .build()
}