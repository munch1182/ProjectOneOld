package com.munch.project.testsimple.jetpack.net

import com.munch.project.testsimple.jetpack.model.Office
import com.munch.project.testsimple.jetpack.model.dto.ArticleWrapper
import com.munch.project.testsimple.jetpack.model.dto.BaseDtoWrapper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Create by munch1182 on 2020/12/17 22:14.
 */
interface Api {


    @GET("/article/list/{page}/json")
    fun getArticleListFlow(@Path("page") page: Int): Flow<BaseDtoWrapper<List<ArticleWrapper>>>

    @GET("/wxarticle/chapters/json  ")
    fun getOfficeList(): Flow<BaseDtoWrapper<Office>>

    @GET("/article/list/{page}/json")
    suspend fun getArticleList(@Path("page") page: Int): BaseDtoWrapper<ArticleWrapper>

    @GET("/article/list/{page}/json")
    fun getArticleListDeferred(@Path("page") page: Int): Deferred<ArticleWrapper>
}