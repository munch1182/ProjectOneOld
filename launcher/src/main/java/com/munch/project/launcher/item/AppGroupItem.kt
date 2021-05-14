package com.munch.project.launcher.item

import android.content.Context
import com.github.promeg.pinyinhelper.Pinyin
import com.munch.project.launcher.R
import com.munch.project.launcher.set.SettingActivity
import java.util.*

/**
 * Create by munch1182 on 2021/5/9 17:24.
 */
data class AppGroupItem(
    val name: String,
    val icon: Any?,
    val pkg: String,
    var launch: String
) : Comparable<AppGroupItem> {

    var letter: Char = ' '
        get() {
            if (field == ' ') {
                field = letters[0]
            }
            return field
        }

    //在该分组下的序列
    var indexInLetter: Int = -1

    //当前位置距离该行末尾的位置，如果不在最后一个，则为1
    //其具体值取决于GridLayout的列数，更新时计算
    var span2End: Int = 1

    private var letters: String = ""
        get() {
            if (field.isEmpty()) {
                val sb = StringBuilder()
                name.toCharArray().forEach {
                    sb.append(Pinyin.toPinyin(it).toUpperCase(Locale.getDefault()))
                }
                field = sb.toString()
            }
            return field
        }

    companion object {

        fun empty(last: Char, indexInLetter: Int, span2End: Int): AppGroupItem {
            return AppGroupItem("", null, "", "").apply {
                this.indexInLetter = indexInLetter
                letter = last
                this.span2End = span2End
            }
        }

        fun set(context: Context, icon: Any?): AppGroupItem {
            return AppGroupItem(
                context.getString(R.string.app_set),
                icon,
                context.packageName,
                SettingActivity::class.java.canonicalName!!
            )
        }

        fun refresh(context: Context, icon: Any?): AppGroupItem {
            return AppGroupItem(
                context.getString(R.string.app_refresh),
                icon,
                context.packageName,
                ""
            )
        }
    }

    override fun compareTo(other: AppGroupItem): Int {
        return letters.compareTo(other.letters)
    }

    override fun toString(): String {
        return "AppGroupItem(name=$name,icon=$icon,pkg=$pkg,launch=$launch,letter=$letter,indexInLetter=$indexInLetter,span2End=$span2End)"
    }

}