package com.munch.project.launcher.app

import com.github.promeg.pinyinhelper.Pinyin
import com.munch.project.launcher.db.AppBean
import com.munch.project.launcher.db.AppSet

/**
 * Create by munch1182 on 2021/2/24 16:01.
 */
data class AppShowBean(
    val latterChar: Char,
    val latterStr: String,
    var showParameter: ShowParameter? = null,
    var isEmpty: Boolean = false,
    val appBean: AppBean
) : Comparable<AppShowBean> {

    companion object {
        private val chars = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

        fun new(appBean: AppBean): AppShowBean {
            var py = Pinyin.toPinyin(appBean.name[0])
            var latterChar = py[0]
            if (latterChar in chars) {
                py = "#"
                latterChar = '#'
            }
            return AppShowBean(latterChar, py, appBean = appBean)
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
        return this.latterStr.compareTo(other.latterStr)
    }
}

data class ShowParameter(
    //在该分组下的序列
    var indexInLetter: Int = 1,
    //当前位置距离该行末尾的位置，如果不在最后一个，则为1
    //其具体值取决于GridLayout的列数，更新时计算
    var space2End: Int = 1,
)