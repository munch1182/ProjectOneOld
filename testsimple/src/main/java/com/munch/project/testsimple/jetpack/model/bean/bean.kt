package com.munch.project.testsimple.jetpack.model.bean

import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.munch.lib.BaseApp
import com.munch.lib.helper.formatDate
import com.munch.project.testsimple.jetpack.model.dto.Tag

/**
 * ben
 * Create by munch1182 on 2021/1/8 13:59.
 */
open class BaseBean {

    override fun toString(): String {
        if (BaseApp.debugMode()) {
            return Gson().toJson(this)
        }
        return super.toString()
    }
}

data class ArticleBean(
    val author: String,
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
    val projectLink: String,
    val publishTime: Long,
    val realSuperChapterId: Int,
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
) : BaseBean() {

    fun showFrom(): String {
        return author.takeIf { author.isNotEmpty() }?.run { "作者:$this" } ?: "分享人:$shareUser"
    }

    fun publishTime2Str(): String {
        return "yyy-MM-dd HH:mm".formatDate(this.publishTime)
    }

    fun showChapter() = "$superChapterName/$chapterName"
}

