package com.munch.project.testsimple.jetpack

import android.os.SystemClock
import androidx.paging.PagingSource
import com.munch.lib.log
import com.munch.project.testsimple.BaseRepository
import com.munch.project.testsimple.jetpack.db.ArticleDao
import com.munch.project.testsimple.jetpack.model.Article
import com.munch.project.testsimple.jetpack.net.Api
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Create by munch1182 on 2020/12/19 15:21.
 */
class ArticleRepository @Inject constructor() : BaseRepository() {

    @Inject
    lateinit var articleDao: ArticleDao

    @Inject
    lateinit var api: Api

    suspend fun getArticleListToday(): Flow<PagingSource<Int, Article>> {
        return flow {
            /*articleDao.insert(Article.testInstance())*/
            delay(1000L)
            val value = articleDao.queryArticle()
            value.asPagingSourceFactory()
            val value1 = value.asPagingSourceFactory().invoke()
            log(value1)
            emit(value1)
        }
    }
}