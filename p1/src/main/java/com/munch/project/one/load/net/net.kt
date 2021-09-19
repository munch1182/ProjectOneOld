package com.munch.project.one.load.net

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2021/9/16 11:35.
 */
//因为此app的网络请求只在某个activity需要，所以用class即可
class Net {

    companion object {

        const val HEADER_OTHER = "${NetChangeBaseUrlInterceptor.NAME_HEADER}:OTHER"

        const val PAGE_SIZE = 15
    }

    private val gson = Gson()

    private val netErrorJson = gson.toJson(NetResult.netError())

    private val okHttpClient = OkHttpClient.Builder()
        .callTimeout(10L, TimeUnit.SECONDS)
        .readTimeout(10L, TimeUnit.SECONDS)
        .addInterceptor(NetChangeBaseUrlInterceptor {
            if (it == HEADER_OTHER) "https://www.biewanandroid.com" else null
        })
        .addInterceptor(NetErrorConvertResInterceptor(netErrorJson))
        /*.addNetworkInterceptor(HttpLoggingInterceptor {
            Log.d("net-loglog", it)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        })*/
        .build()

    private val retrofit = Retrofit.Builder().client(okHttpClient)
        .baseUrl("https://www.wanandroid.com")
        .addCallAdapterFactory(
            NetTransformCallAdapterFactory.create()
        )
        .addCallAdapterFactory(NetFlowCallAdapterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val server: Server = retrofit.create(Server::class.java)
}

class NetResultTransformer : NetTransformer<NetResult<Any?>, Any?> {
    override fun transform(from: NetResult<Any?>): Any? {
        return from.data
    }
}

//todo
class NetArticleNoPageTransformer : NetTransformer<NetResult<ArticlesWrapper?>, List<Articles>?> {
    override fun transform(from: NetResult<ArticlesWrapper?>): List<Articles>? {
        return from.data?.data
    }
}

interface Server {

    /** 一般协程返回 */
    @GET("/article/list/{page}/json")
    suspend fun queryArticle(
        @Path("page") page: Int,
        @Query("page_size") size: Int = Net.PAGE_SIZE
    ): NetResult<ArticlesWrapper>

    /** 去掉了固定的NetResult直接返回data */
    @GET("/article/list/{page}/json")
    //使用全局转换
    @NetDataTransformer(NetResult::class, NetResultTransformer::class)
    suspend fun queryArticleOnlyData(
        @Path("page") page: Int,
        @Query("page_size") size: Int = Net.PAGE_SIZE
    ): ArticlesWrapper?

    /** 返回flow格式的数据 */
    @GET("/article/list/{page}/json")
    fun queryArticle2Flow(
        @Path("page") page: Int,
        @Query("page_size") size: Int = Net.PAGE_SIZE
    ): Flow<NetResult<ArticlesWrapper>>

    /** 返回不带code的flow格式的数据 */
    @GET("/article/list/{page}/json")
    fun queryArticle2FlowNoCode(
        @Path("page") page: Int,
        @Query("page_size") size: Int = Net.PAGE_SIZE
    ): Flow<ArticlesWrapper>

    /** 测试请求网址错误 */
    @GET("/no_the_query_url")
    suspend fun testUrlError(): NetResult<String>

    /** 测试解析错误 */
    @GET("/article/list/0/json")
    suspend fun testParseError(): NetResult<String>

    @GET("/article/list/0/json")
    @Headers(Net.HEADER_OTHER)
    suspend fun testOtherBaseUrl(): NetResult<String>

    @GET("/article/list/0/json")
    suspend fun testResultStr(@Query("page_size") size: Int = 1): String
}

data class NetResult<T>(val errorCode: Int, val errorMsg: String, val data: T?) {

    companion object {

        private const val CODE_SUCCESS = 0
        private const val CODE_FAIL_NET = 999

        /**
         * 该错误为网络错误
         */
        fun netError() = NetResult<String>(CODE_FAIL_NET, "网络错误", null)
    }

    val isSuccessful: Boolean
        get() = errorCode == CODE_SUCCESS

    val isNetError: Boolean
        get() = errorCode == CODE_FAIL_NET

}

data class ArticlesWrapper(val curPage: Int, @SerializedName("datas") val data: List<Articles>?)

data class Articles(
    val title: String,
    val author: String?,
    val shareUser: String?,
    val chapterName: String?,
    val link: String
)