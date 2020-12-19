package com.munch.project.testsimple.jetpack.model

import androidx.room.Entity
import androidx.room.PrimaryKey
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

@Entity
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
    /*val tags: List<Tag>,*/
    val title: String,
    val type: Int,
    val userId: Int,
    val visible: Int,
    val zan: Int
) {

    fun getPublishTime(): String {
        return "yyyMMdd HH:mm".formatDate(this.publishTime)
    }
}

data class Tag(
    val name: String,
    val url: String
)