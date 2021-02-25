package com.munch.project.launcher.app

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
            val py = Pinyin.toPinyin(appBean.name[0])
            val letterChar = py[0]
            if (letterChar.isDigit()) {
                return AppShowBean(charNum, charNum.toString(), appBean = appBean)
            }
            return AppShowBean(letterChar, py, appBean = appBean)
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
        if (this.letterChar == other.letterChar) {
            return 0
        }
        if (this.letterChar == '#') {
            return 1
        }
        if (other.letterChar == '#') {
            return -1
        }
        return this.letterStr.toUpperCase(Locale.ROOT)
            .compareTo(other.letterStr.toUpperCase(Locale.ROOT))
    }
}

data class ShowParameter(
    //在该分组下的序列
    var indexInLetter: Int = -1,
    //当前位置距离该行末尾的位置，如果不在最后一个，则为1
    //其具体值取决于GridLayout的列数，更新时计算
    var space2End: Int = 1,
)