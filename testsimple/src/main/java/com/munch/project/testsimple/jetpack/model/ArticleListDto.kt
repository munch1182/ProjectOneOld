package com.munch.project.testsimple.jetpack.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.munch.lib.helper.formatDate

/**
 * Create by munch1182 on 2020/12/17 21:43.
 */

data class ArticleWrapper(
    val curPage: Int,
    val datas: List<Article>,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int
)

/**
 * 如果数据简单，可以用TypeConverters直接处理list
 */
@Entity
@TypeConverters(TagConverters::class)
data class Article(
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
) {

    companion object {
        private const val DATA_TEST = "{apkLink: \"\"," +
                "audit: 1," +
                "author: \"author\"," +
                "canEdit: false," +
                "chapterId: 502," +
                "chapterName: 自助," +
                "collect: false," +
                "courseId: 13," +
                "desc: \"\"," +
                "descMd: \"\"," +
                "envelopePic: \"\"," +
                "fresh: false," +
                "id: 16470," +
                "link: \"https://juejin.cn/post/6906153878312452103\"," +
                "niceDate: \"2020-12-15 10:03\"," +
                "niceShareDate: \"2020-12-15 10:03\"," +
                "origin: \"\"," +
                "prefix: \"\"," +
                "projectLink: \"\"," +
                "publishTime: 1607997828000," +
                "realSuperChapterId: 493," +
                "selfVisible: 0," +
                "shareDate: 1607997828000," +
                "shareUser: DylanCai," +
                "superChapterId: 494," +
                "superChapterName:\"广场Tab\"," +
                "tags: []," +
                "title: \"优雅地封装和使用 ViewBinding，该替代 Kotlin synthetic 和 ButterKnife 了\"," +
                "type: 0," +
                "userId: 27680," +
                "visible: 1," +
                "zan: 0}"

        fun testInstance(): Article {
            return Gson().fromJson(DATA_TEST, Article::class.java)
        }
    }

    fun publishTime2Str(): String {
        return "yyyMMdd HH:mm".formatDate(this.publishTime)
    }

    fun showChapter() = "$superChapterName/$chapterName"
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