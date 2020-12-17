package com.munch.project.testsimple.jetpack.net

import com.munch.project.testsimple.jetpack.model.ArticleListDto
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Create by munch1182 on 2020/12/17 22:14.
 */
interface Api {


    @GET("/article/list/{page}/json")
    fun getArticleList(@Path("page") page: Int): Flow<ArticleListDto>

}