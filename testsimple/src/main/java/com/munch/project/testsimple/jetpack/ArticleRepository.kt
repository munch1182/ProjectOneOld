package com.munch.project.testsimple.jetpack

import androidx.paging.PagingSource
import com.munch.project.testsimple.BaseRepository
import com.munch.project.testsimple.jetpack.db.ArticleDao
import com.munch.project.testsimple.jetpack.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Create by munch1182 on 2020/12/19 15:21.
 */
class ArticleRepository @Inject constructor() : BaseRepository() {

    @Inject
    lateinit var articleDao: ArticleDao

    fun getArticleListToday(): Flow<PagingSource<Int, Article>> {
        return flow {
            articleDao.queryArticle().asPagingSourceFactory()
        }
    }
}