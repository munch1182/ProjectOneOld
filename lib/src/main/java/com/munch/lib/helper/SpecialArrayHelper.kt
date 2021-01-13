package com.munch.lib.helper

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

    private var array = arrayListOf<Float>()

    fun setArray(array: Collection<Float>) {
        this.array.clear()
        this.array.addAll(array)
    }

    open fun addArray(vararg value: Float) {
        this.array.addAll(value.toTypedArray())
    }

    open fun getVal(level: Int, index: Int): Float {
        return array[level * unit + index]
    }

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