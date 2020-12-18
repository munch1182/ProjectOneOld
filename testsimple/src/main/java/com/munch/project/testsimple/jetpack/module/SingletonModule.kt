package com.munch.project.testsimple.jetpack.module

import com.google.gson.Gson
import com.munch.lib.log
import com.munch.project.testsimple.jetpack.net.Api
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Create by munch1182 on 2020/12/17 21:46.
 */
@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    private const val BASE_URL = "https://www.wanandroid.com"

    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            /*.addCallAdapterFactory(FlowCallAdapterFactory())*/
            .client(client)
            .build()
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                addInterceptor(HttpLoggingInterceptor {
                    log(it)
                }.apply {
                    setLevel(HttpLoggingInterceptor.Level.BODY)
                })
            }
            .addInterceptor { chain ->
                var time = System.currentTimeMillis()
                val request = chain.request()
                log("request：$request.url")
                val response = chain.proceed(request.newBuilder().build())
                time = System.currentTimeMillis() - time
                log("response：${time}ms,${response.body?.string()}")
                response
            }.build()
    }

    @Provides
    fun provideApi(retrofit: Retrofit): Api = retrofit.create(Api::class.java)
}