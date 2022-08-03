package com.munch.project.one.web

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.munch.lib.extend.lazy

open class Net {

    protected open val clientBuilder = OkHttpClient.Builder()
    protected open val retrofitBuilder: Retrofit.Builder by lazy {
        Retrofit.Builder()
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
    }

    protected open val retrofit: Retrofit by lazy { retrofitBuilder.build() }

}