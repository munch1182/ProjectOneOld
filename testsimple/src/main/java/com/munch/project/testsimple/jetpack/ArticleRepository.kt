package com.munch.project.testsimple.jetpack

import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.munch.project.testsimple.jetpack.db.ArticleDao
import com.munch.project.testsimple.jetpack.model.Article
import com.munch.project.testsimple.jetpack.net.Api
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Create by munch1182 on 2020/12/19 15:21.
 */
class ArticleRepository @Inject constructor() : BaseRepository() {

    @Inject
    lateinit var articleDao: ArticleDao

    @Inject
    lateinit var api: Api

    @WorkerThread
    fun getArticleListToday(): Flow<PagingData<Article>> {
        /* api.getArticleList()*/
        return Pager(
            PagingConfig(pageSize = 20),
            initialKey = 0,
            pagingSourceFactory = articleDao.queryArticle().asPagingSourceFactory()
            /*remoteMediator = RemoteMediator*/
        ).flow
    }

    /*@ExperimentalPagingApi
    class ArticlePagingSource @Inject constructor() : RemoteMediator<Int, Article>() {

        @Inject
        lateinit var api: Api
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, Article>
        ): MediatorResult {
        }
    }*/
}