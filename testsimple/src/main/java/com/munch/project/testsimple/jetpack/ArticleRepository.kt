package com.munch.project.testsimple.jetpack

import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.munch.project.testsimple.jetpack.db.ArticleDao
import com.munch.project.testsimple.jetpack.model.dto.ArticleDto
import com.munch.project.testsimple.jetpack.net.Api
import com.munch.project.testsimple.jetpack.net.noWrapper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 整体的思路是：所有的页面请求都通过此类
 * Create by munch1182 on 2020/12/19 15:21.
 */

class ArticleRepository @Inject constructor() : BaseRepository() {

    @Inject
    lateinit var articleDao: ArticleDao

    @Inject
    lateinit var api: Api

    /**
     * 注意：pager.flow是一个collect然后重新发送的过程，因此即使在非主线程调用，pagingSourceFactory仍然遵照自己的线程
     */
    @WorkerThread
    suspend fun getArticleListToday(): Flow<PagingData<ArticleDto>> {
        val pagingSourceFactory = {
            object : PagingSource<Int, ArticleDto>() {
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleDto> {
                    val page = params.key ?: 0
                    return try {
                        val wrapper = api.getArticleList(page).noWrapper()
                        LoadResult.Page(
                            wrapper.datas,
                            null,
                            page + 1,
                            wrapper.offset,
                            wrapper.total - wrapper.offset
                        )
                        /*LoadResult.Page(api.getArticleListDeferred(page).await().datas, null, page + 1)*/
                    } catch (e: Exception) {
                        LoadResult.Error(e)
                    }
                }
            }
        }
        return Pager(
            PagingConfig(pageSize = 20),
            initialKey = 0,
            pagingSourceFactory = pagingSourceFactory,
            /*remoteMediator = ArticlePagingSource()*/
        ).flow
    }

    /*@ExperimentalPagingApi
    @UNCOMPLETE
    class ArticlePagingSource @Inject constructor() : RemoteMediator<Int, Article>() {

        @Inject
        lateinit var api: Api

        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, Article>
        ): MediatorResult {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(true)
                LoadType.APPEND -> {

                }
            }
            return try {
                MediatorResult.Success()
            } catch (e: Exception) {
                MediatorResult.Error(e)
            }
        }

    }*/
}