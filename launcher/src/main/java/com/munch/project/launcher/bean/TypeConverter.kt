package com.munch.project.launcher.bean

/**
 * Create by munch1182 on 2021/2/23 11:51.
 */
interface StringConverter {

    companion object {
        private const val SPLIT = ","
        fun splitStr(string: String): List<String> {
            return string.substring(0, string.length - 1).split(SPLIT)
        }

        /**
         * 将list转为string类型
         */
        fun convertList2String(list: List<String>): String {
            return "[${convert2Str(list)}]"
        }

        private fun convert2Str(list: List<Any?>): String {
            return StringBuilder().apply {
                list.forEachIndexed { index, s ->
                    append(s?.toString())
                    if (index != list.size - 1) {
                        append(SPLIT)
                    }
                }
            }.toString()
        }

    }

    /**
     * 将当前类转为string类型
     *
     * @param list 当前类的属性组合成的list
     */
    fun convert2String(list: List<Any?>): String {
        return "{${convert2Str(list)}}"
    }


}