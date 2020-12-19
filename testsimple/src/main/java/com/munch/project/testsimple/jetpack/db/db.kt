package com.munch.project.testsimple.jetpack.db

import androidx.paging.DataSource
import androidx.room.*
import com.munch.project.testsimple.jetpack.model.Article

/**
 * Create by munch1182 on 2020/12/19 15:30.
 */
@Database(entities = [Article::class], version = 1)
abstract class Db : RoomDatabase() {

    abstract fun articleDao(): ArticleDao

}

@Dao
interface ArticleDao {

    @Query("SELECT * FROM article ORDER BY publishTime DESC")
    fun queryArticle(): DataSource.Factory<Int, Article>

    /**
     * 或者将模糊搜索传[like]之前给其前后加上%
     */
    //"SELECT * FROM article WHERE title + author LIKE '%' || ':like' || '%' ORDER BY publishTime DESC"
    @Query("SELECT * FROM article"/*WHERE title LIKE '%' || ':like' || '%' OR author LIKE '%' || ':like' || '%' ORDER BY publishTime DESC"*/)
    fun queryArticleByLike(like: String): DataSource.Factory<Int, Article>

    @Delete
    fun del(article: Article)

    @Insert
    fun insert(vararg article: Article)
}