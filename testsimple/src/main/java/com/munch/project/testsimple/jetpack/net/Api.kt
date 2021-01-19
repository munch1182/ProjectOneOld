package com.munch.project.testsimple.jetpack.net

import com.munch.lib.extend.retrofit.ApiResult
import com.munch.project.testsimple.jetpack.model.dto.ArticleDto
import com.munch.project.testsimple.jetpack.model.dto.ArticleWrapperDto
import com.munch.project.testsimple.jetpack.model.dto.BaseDtoWrapper
import com.munch.project.testsimple.jetpack.model.dto.Office
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Create by munch1182 on 2020/12/17 22:14.
 */
interface Api {


    /**
     * @see com.munch.lib.extend.retrofit.FlowCallAdapterFactory
     *
     * 将返回值直接转为flow
     */
    @GET("/article/list/{page}/json")
    fun getArticleListFlow(@Path("page") page: Int): Flow<BaseDtoWrapper<List<ArticleDto>>>

    @GET("/wxarticle/chapters/json")
    fun getOfficeList(): Flow<BaseDtoWrapper<Office>>

    @GET("/article/list/{page}/json")
    fun getArticleListDeferred(@Path("page") page: Int): Deferred<ArticleWrapperDto>

    @GET("/article/list/{page}/json")
    suspend fun getArticleList(@Path("page") page: Int): BaseDtoWrapper<ArticleWrapperDto>


    /**
     * @see com.munch.lib.extend.retrofit.ApiResultCallAdapterFactory
     *
     * 普通的包装实现
     */
    @GET("/article/list/{page}/json")
    suspend fun getArticleList2(@Path("page") page: Int): ApiResult<BaseDtoWrapper<ArticleWrapperDto>>

    /**
     * @see ApiResultNoWrapperCallAdapterFactory
     *
     * 无需声明[BaseDtoWrapper]的实现
     */
    @GET("/article/list/{page}/json")
    suspend fun getArticleList3(@Path("page") page: Int): ApiResult<ArticleWrapperDto>
}