package com.munch.project.testsimple.jetpack

import androidx.annotation.WorkerThread
import androidx.paging.*
import com.munch.lib.log
import com.munch.project.testsimple.jetpack.db.ArticleDao
import com.munch.project.testsimple.jetpack.db.Db
import com.munch.project.testsimple.jetpack.db.PageDao
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
    lateinit var pageDao: PageDao

    @Inject
    lateinit var api: Api

    @Inject
    lateinit var db: Db

    companion object {
        /*初始id得根据数据中的值来定*/
        internal const val INITIAL_KEY = 1
        internal const val PAGE_SIZE = 20
    }

    /**
     * 注意：pager.flow是一个collect然后重新发送的过程，因此即使在非主线程调用，pagingSourceFactory仍然遵照自己的线程
     */
    @WorkerThread
    @ExperimentalPagingApi
    fun getArticleListToday(): Flow<PagingData<ArticleDto>> {
        /*1. 直接将服务器数据当作数据源*/
        /*val pagingSourceFactory = {
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
                        *//*LoadResult.Page(api.getArticleListDeferred(page).await().datas, null, page + 1)*//*
                    } catch (e: Exception) {
                        LoadResult.Error(e)
                    }
                }
            }
        }*/
        /*2. 直接将room当作数据源，此时remoteMediator为null*/
        /*val pagingSourceFactory = articleDao.queryArticle().asPagingSourceFactory()*/
        /*3. 将room当作数据源，服务器通过更新room的方式更新数据源*/
        /*此方法的关键在于pagingSourceFactory能判断并设置请求失效的情形，用自行构造的LoadResult是无法处理的*/
        /*room数据库返回的DataSource.Factory能自动判断并设置无数据时的失效，因此可以在无数据时调用remoteMediator*/
        val pagingSourceFactory = articleDao.queryArticle().asPagingSourceFactory()

        return Pager(
            PagingConfig(pageSize = PAGE_SIZE),
            initialKey = INITIAL_KEY,
            pagingSourceFactory = pagingSourceFactory,
            remoteMediator = ArticlePagingSource(api, pageDao, articleDao)
        ).flow
    }

    @ExperimentalPagingApi
    class ArticlePagingSource constructor(
        private val api: Api,
        private val pageDao: PageDao,
        private val articleDao: ArticleDao
    ) : RemoteMediator<Int, ArticleDto>() {

        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, ArticleDto>
        ): MediatorResult {
            return try {
                var loadKey = when (loadType) {
                    LoadType.REFRESH -> {
                        INITIAL_KEY
                    }
                    LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                    LoadType.APPEND -> {
                        (pageDao.queryLast()?.page?.curPage ?: INITIAL_KEY - 1) + 1
                    }
                }
                if (loadKey < 1) {
                    loadKey = 1
                }
                //因为服务端api的页数实际上从0开始但返回值是从1开始
                val noWrapper = api.getArticleList(loadKey - 1).noWrapper()
                val datas = noWrapper.datas
                if (loadType == LoadType.REFRESH) {
                    //刷新时如果有新数据则清除数据库，否则重用数据库的数据
                    //可以考虑一种更高效的方式或者结构插入新值
                    //比如对比插入新值然后根据id大小排序
                    if (datas?.get(0)?.equals(articleDao.queryArticleFirst()) != true) {
                        pageDao.delAll()
                    }
                }
                pageDao.insert(noWrapper)
                if (datas?.isEmpty() == false) {
                    articleDao.insert(datas)
                    MediatorResult.Success(false)
                } else {
                    MediatorResult.Success(true)
                }
            } catch (e: Exception) {
                log(e.message)
                MediatorResult.Error(e)
            }
        }

    }
}