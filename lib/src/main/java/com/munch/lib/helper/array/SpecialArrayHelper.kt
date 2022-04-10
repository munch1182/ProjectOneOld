package com.munch.lib.helper.array


/**
 * 用array来替代大量的rect或者类似对象
 * 来避免比如绘制时的大量对象创建
 *
 * 每[unit]为一组数据，因此只要记得排序顺序即可通过[getVal]取出一组数据中的第几个数据来代替重复对象
 *
 * 排序顺序建议方法化
 *
 * Create by munch1182 on 2021/1/10 1:32.
 */
interface SpecialArray<N : Number> {

    val unit: Int
    val array: MutableList<N>

    fun clear() {
        array.clear()
    }

    /**
     * @param array 设置的数据，必须按照[unit]一组
     */
    fun set(array: Collection<N>) {
        clear()
        this.array.addAll(array)
    }


    /**
     * @param value 添加的数据，必须按照[unit]一组
     */
    fun add(vararg value: N) {
        this.array.addAll(value.asList())
    }

    fun getVal(level: Int, index: Int): N {
        return array[level * unit + index]
    }

    fun getCount() = array.size / unit
}

open class SpecialIntArrayHelper(override val unit: Int) : SpecialArray<Int> {

    private val num = mutableListOf<Int>()

    override val array: MutableList<Int>
        get() = num
}

open class SpecialFloatArrayHelper(override val unit: Int) : SpecialArray<Float> {

    private val num = mutableListOf<Float>()

    override val array: MutableList<Float>
        get() = num
}
