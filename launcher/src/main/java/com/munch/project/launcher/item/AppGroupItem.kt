package com.munch.project.launcher.item

/**
 * Create by munch1182 on 2021/5/9 17:24.
 */
data class AppGroupItem(val name: String, val icon: Any?) : Comparator<AppGroupItem> {

    var letter: Char = '?'

    companion object {
        fun empty() = AppGroupItem("", null)
    }

    override fun compare(o1: AppGroupItem?, o2: AppGroupItem?): Int {
        o1 ?: return 1
        o2 ?: return -1
        return o1.letter.compareTo(o2.letter)
    }
}