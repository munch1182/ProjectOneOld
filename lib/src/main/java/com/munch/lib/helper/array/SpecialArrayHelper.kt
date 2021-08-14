@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper.array

/**
 * 用array来替代大量的rect或者类似对象
 * 来避免比如绘制时的大量对象创建
 *
 * 每[unit]为一组数据，因此只要记得排序顺序即可通过[getVal]取出一组数据中的第几个数据来代替重复对象
 *
 * 排序顺序建议方法化，参见[RectArrayHelper]
 *
 * Create by munch1182 on 2021/1/10 1:32.
 */
open class SpecialArrayHelper(private val unit: Int) {

    private var array = mutableListOf<Int>()

    /**
     * @param array 设置的数据，必须按照[unit]一组
     */
    fun set(array: Collection<Int>) {
        clear()
        this.array.addAll(array)
    }

    /**
     * @param value 添加的数据，必须按照[unit]一组
     */
    open fun add(vararg value: Int) {
        this.array.addAll(value.asList())
    }

    open fun getVal(level: Int, index: Int): Int {
        return array[level * unit + index]
    }

    open fun clear() {
        array.clear()
    }

    open fun getArray() = array

    open fun getCount() = array.size / unit

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("array(\n")
        var level = 0
        array.forEach {
            sb.append("$it, ")
            level++
            if (level > 0 && level % unit == 0) {
                sb.append("\n")
            }
        }
        sb.append(")")
        return sb.toString()
    }
}