package com.munch.project.testsimple.jetpack.net

import com.munch.project.testsimple.jetpack.model.ArticleWrapper
import com.munch.project.testsimple.jetpack.model.BaseDtoWrapper
import com.munch.project.testsimple.jetpack.model.Office
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Create by munch1182 on 2020/12/17 22:14.
 */
interface Api {


    @GET("/article/list/{page}/json")
    fun getArticleList(@Path("page") page: Int): Flow<BaseDtoWrapper<ArticleWrapper>>

    @GET("/wxarticle/chapters/json  ")
    fun getOfficeList(): Flow<BaseDtoWrapper<Office>>
}