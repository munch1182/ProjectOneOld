package com.munch.project.launcher.appitem

import com.github.promeg.pinyinhelper.Pinyin
import com.munch.project.launcher.db.AppBean
import com.munch.project.launcher.db.AppSet
import java.util.*

/**
 * Create by munch1182 on 2021/2/24 16:01.
 */
data class AppShowBean(
    val letterChar: Char,
    val letterStr: String,
    var showParameter: ShowParameter? = null,
    var isEmpty: Boolean = false,
    val appBean: AppBean
) : Comparable<AppShowBean> {

    companion object {

        private const val charNum = '#'

        fun new(appBean: AppBean): AppShowBean {
            if (appBean.name.isEmpty() || appBean.name[0].isDigit()) {
                return AppShowBean(charNum, charNum.toString(), appBean = appBean)
            }
            var py = ""
            appBean.name.toCharArray().forEach {
                py += Pinyin.toPinyin(it)
            }
            return AppShowBean(py[0], py, appBean = appBean)
        }

        fun empty(letterChar: Char, parameter: ShowParameter): AppShowBean {
            return AppShowBean(
                letterChar,
                letterChar.toString(),
                parameter,
                true,
                AppBean(name = "", pkgName = "", set = AppSet.def(-1))
            )
        }
    }

    fun updateShowParameter(parameter: ShowParameter? = null): AppShowBean {
        showParameter = parameter
        return this
    }

    override fun compareTo(other: AppShowBean): Int {
        if (this.letterStr == other.letterStr) {
            return 0
        }
        if (this.letterChar == '#') {
            return 1
        }
        if (other.letterChar == '#') {
            return -1
        }
        val theArray = this.letterStr.toUpperCase(Locale.ROOT).toCharArray()
        val otherArray = other.letterStr.toUpperCase(Locale.ROOT).toCharArray()
        val theSize = theArray.size
        val otherSize = otherArray.size
        if (theSize == otherSize) {
            return this.letterChar.compareTo(other.letterChar)
        }
        repeat(theSize.coerceAtMost(otherSize)) {
            val res = theArray[it].compareTo(otherArray[it])
            if (res != 0) {
                return res
            }
        }
        return theSize - otherSize
    }
}

data class ShowParameter(
    //在该分组下的序列
    var indexInLetter: Int = -1,
    //当前位置距离该行末尾的位置，如果不在最后一个，则为1
    //其具体值取决于GridLayout的列数，更新时计算
    var space2End: Int = 1,
)