package com.munch.project.testsimple.jetpack.model.dto

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.munch.project.testsimple.jetpack.db.Db
import com.munch.project.testsimple.jetpack.db.PageDao
import com.munch.project.testsimple.jetpack.model.bean.ArticleBean
import com.munch.project.testsimple.jetpack.model.bean.Dto2BeanConvert

/**
 * Create by munch1182 on 2020/12/17 21:43.
 */

/**
 * 关系映射
 *
 * 要这样查询，需要查询的表是"tb_page"，返回值是[PageArticle]
 * 参见[PageDao.queryArticle]
 */
data class PageArticle(
    @Embedded var page: ArticleWrapperDto,
    @Relation(
        parentColumn = "curPage", //这个参数来自于@Embedded指定的表
        entityColumn = "id", // 这个参数来自于entity指定的ArticleDto
        entity = ArticleDto::class,
        associateBy = Junction(PageWithArticle::class)// 映射关系
    )
    var article: List<ArticleDto>
)

@Entity(tableName = Db.TB_NAME_PAGE)
data class ArticleWrapperDto(
    @PrimaryKey
    val curPage: Int,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int
) {
    /**
     * 因为要传入数据库，所以不能当作构造，因为要服务器返回传值，所以不能省略
     * 实际上应该将返回的dto与数据库的bean类分开
     */
    @Ignore
    val datas: List<ArticleDto>? = null

}

@Entity(tableName = Db.TB_NAME_PAGE_WITH_ARTICLE, primaryKeys = ["curPage", "id"])
data class PageWithArticle(val curPage: Int, val id: Int)

/**
 * 如果数据简单，可以用TypeConverters直接处理list
 */
@Entity(tableName = Db.TB_NAME_ARTICLE)
@TypeConverters(TagConverters::class)
data class ArticleDto(
    val apkLink: String,
    val audit: Int,
    val author: String,
    val canEdit: Boolean,
    val chapterId: Int,
    val chapterName: String,
    val collect: Boolean,
    val courseId: Int,
    val desc: String,
    val descMd: String,
    val envelopePic: String,
    val fresh: Boolean,
    @PrimaryKey val id: Int,
    val link: String,
    val niceDate: String,
    val niceShareDate: String,
    val origin: String,
    val prefix: String,
    val projectLink: String,
    val publishTime: Long,
    val realSuperChapterId: Int,
    val selfVisible: Int,
    val shareDate: Long,
    val shareUser: String,
    val superChapterId: Int,
    val superChapterName: String,
    //通过TypeConverters，tags在sql中类型变成了TEXT
    //但取出来之后会被自然转换为List
    val tags: List<Tag>,
    val title: String,
    val type: Int,
    val userId: Int,
    val visible: Int,
    val zan: Int
) : Dto2BeanConvert<ArticleBean> {
    override fun convert(): ArticleBean {
        return ArticleBean(
            author,
            chapterId,
            chapterName,
            collect,
            courseId,
            desc,
            descMd,
            envelopePic,
            fresh,
            id,
            link,
            niceDate,
            niceShareDate,
            projectLink,
            publishTime,
            realSuperChapterId,
            shareDate,
            shareUser,
            superChapterId,
            superChapterName,
            tags,
            title,
            type,
            userId,
            visible,
            zan
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is ArticleDto) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }
}

@Entity
data class Tag(
    val name: String,
    val url: String
)

class TagConverters {

    @TypeConverter
    fun str2Tags(str: String): List<Tag> {
        return try {
            Gson().fromJson(str, object : TypeToken<List<Tag>>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    @TypeConverter
    fun tags2Str(tags: List<Tag>): String = try {
        Gson().toJson(tags)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}