package com.munch.project.testsimple.jetpack.db

import androidx.paging.DataSource
import androidx.room.*
import com.munch.project.testsimple.jetpack.model.dto.ArticleDto
import com.munch.project.testsimple.jetpack.model.dto.ArticleWrapperDto
import com.munch.project.testsimple.jetpack.model.dto.PageArticle
import com.munch.project.testsimple.jetpack.model.dto.PageWithArticle

/**
 * 数据库升级语句见[com.munch.project.testsimple.jetpack.module.SingletonModule.Migration1to2]的Migration
 * 如果exportSchema则数据类型见schemas
 *
 * version变化，同时要注意[ArticleDto]中字段的变化
 *
 * Create by munch1182 on 2020/12/19 15:30.
 */
@Database(
    entities = [ArticleDto::class, ArticleWrapperDto::class, PageWithArticle::class],
    version = 2,
    exportSchema = true
)
abstract class Db : RoomDatabase() {

    abstract fun articleDao(): ArticleDao

    abstract fun pageDao(): PageDao

}

@Dao
interface PageDao {

    /*查询的是带关系的结果，需要@Transaction注解*/
    @Transaction
    @Query("SELECT * FROM tb_page WHERE curPage = :page LIMIT 1")
    suspend fun queryArticle(page: Int): PageArticle?

    /*内联多表查询，但是无法转换为DataSource.Factory*/
    @Query("SELECT * FROM tb_article as art INNER JOIN tb_page_with_article as pa ON art.id = pa.id WHERE pa.curPage = :page ")
    suspend fun queryArticleByPage(page: Int): List<ArticleDto>

    @Transaction
    @Query("SELECT * FROM tb_page ORDER BY curPage DESC LIMIT 1")
    suspend fun queryLast(): PageArticle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPageArticle(wrapper: List<PageWithArticle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(pageWithArticle: ArticleWrapperDto)

    @Transaction
    suspend fun insert(wrapper: ArticleWrapperDto) {
        insertPage(wrapper)
        insertPageArticle(wrapper.datas?.map {
            PageWithArticle(wrapper.curPage, it.id)
        } ?: return)
    }

    @Transaction
    suspend fun delAll() {
        clearArticle()
        clearPage()
        clearPageWithArticle()
    }

    @Query("DELETE FROM tb_article")
    suspend fun clearArticle()

    @Query("DELETE FROM tb_page")
    suspend fun clearPage()

    @Query("DELETE FROM tb_page_with_article")
    suspend fun clearPageWithArticle()
}

/**
 * 对于数据库来说，返回的应该是数据结构，而不应该返回其他需要构造的类型
 * 需要构造的类型应该交由调用方自行构造
 */
@Dao
interface ArticleDao {

    @Query("SELECT * FROM tb_article  ORDER BY publishTime DESC LIMIT :pageSize OFFSET :page ")
    fun queryArticle(page: Int, pageSize: Int): DataSource.Factory<Int, ArticleDto>

    @Query("SELECT * FROM tb_article ORDER BY publishTime DESC ")
    fun queryArticle(): DataSource.Factory<Int, ArticleDto>

    @Query("SELECT * FROM tb_article ORDER BY publishTime DESC LIMIT 1")
    suspend fun queryArticleFirst(): ArticleDto

    /**
     * 或者将模糊搜索传[like]之前给其前后加上%
     */
    @Query("SELECT * FROM tb_article WHERE title || author LIKE '%' || :like || '%' ORDER BY publishTime DESC")
    fun queryArticleByLike(like: String): DataSource.Factory<Int, ArticleDto>

    @Delete
    fun del(article: ArticleDto)

    @Query("DELETE FROM tb_article")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg article: ArticleDto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: List<ArticleDto>)
}