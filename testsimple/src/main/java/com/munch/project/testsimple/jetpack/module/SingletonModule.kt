package com.munch.project.testsimple.jetpack.module

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.gson.Gson
import com.munch.lib.log
import com.munch.project.testsimple.jetpack.db.Db
import com.munch.project.testsimple.jetpack.net.Api
import com.munch.project.testsimple.jetpack.net.FlowCallAdapterFactory
import com.munch.project.testsimple.jetpack.net.uncomplete.FlowNoWrapperCallAdapterFactory
import com.munch.project.testsimple.jetpack.net.uncomplete.NoWrapperConverterFactory
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
    private const val NAME_DB = "db_article"

    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .addCallAdapterFactory(FlowCallAdapterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                addInterceptor(HttpLoggingInterceptor { log(it) }.apply {
                    setLevel(HttpLoggingInterceptor.Level.BODY)
                })
            }
            .build()
    }

    @Provides
    fun provideApi(retrofit: Retrofit): Api = retrofit.create(Api::class.java)

    @Provides
    fun provideDb(app: Application): Db {
        return Room.databaseBuilder(app, Db::class.java, NAME_DB).build()
    }

    @Provides
    fun provideArticleDao(db: Db) = db.articleDao()
}