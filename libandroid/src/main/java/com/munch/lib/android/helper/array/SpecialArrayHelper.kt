package com.munch.lib.android.helper.array

/**
 * 通过使用[array]来记录按照[unit]个元素为一组的重复数据, 避免反复建立对象
 *
 * 更建议实现时以有名称的方法来返回元素
 *
 * Create by munch1182 on 2021/1/10 1:32.
 */
interface SpecialArray<N> {

    val unit: Int
    val array: MutableList<N>

    val size: Int
        get() = array.size / unit

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

    /**
     * 获取第[level]组的第[index]个的数据
     */
    fun getVal(level: Int, index: Int): N {
        return array[level * unit + index]
    }

    fun getCount() = array.size / unit

    fun getVal(index: Int) = array[index]
}

open class SpecialArrayHelper<N>(override val unit: Int) : SpecialArray<N> {

    protected open val num = mutableListOf<N>()

    override val array: MutableList<N>
        get() = num
}
