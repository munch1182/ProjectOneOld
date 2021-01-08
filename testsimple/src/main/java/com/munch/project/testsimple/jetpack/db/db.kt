package com.munch.project.testsimple.jetpack.db

import androidx.paging.DataSource
import androidx.room.*
import com.munch.project.testsimple.jetpack.model.dto.ArticleDto

/**
 * 数据库升级语句见[com.munch.project.testsimple.jetpack.module.SingletonModule.Migration1to2]的Migration
 * 如果exportSchema则数据类型见schemas
 *
 * version变化，同时要注意[ArticleDto]中字段的变化
 *
 * Create by munch1182 on 2020/12/19 15:30.
 */
@Database(entities = [ArticleDto::class], version = 2, exportSchema = true)
abstract class Db : RoomDatabase() {

    abstract fun articleDao(): ArticleDao

}

/**
 * 对于数据库来说，返回的应该是数据结构，而不应该返回其他需要构造的类型
 * 需要构造的类型应该交由调用方自行构造
 */
@Dao
interface ArticleDao {

    @Query("SELECT * FROM ArticleDto ORDER BY publishTime DESC")
    fun queryArticle(): DataSource.Factory<Int, ArticleDto>

    /**
     * 或者将模糊搜索传[like]之前给其前后加上%
     */
    @Query("SELECT * FROM ArticleDto WHERE title || author LIKE '%' || :like || '%' ORDER BY publishTime DESC")
    fun queryArticleByLike(like: String): DataSource.Factory<Int, ArticleDto>

    @Delete
    fun del(article: ArticleDto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg article: ArticleDto)
}