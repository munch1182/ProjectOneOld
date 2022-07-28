package com.munch.lib.recyclerview


/**
 * adapter的数据中不应该包括null，如果需要为null的，需要删除，或者使用字段来代表
 *
 * Create by munch1182 on 2021/12/6 16:24.
 */
interface IAdapterFun<D> {

    val itemSize: Int

    fun set(newData: List<D>?)

    fun add(element: D)
    fun add(index: Int, element: D)
    fun add(elements: Collection<D>)

    /**
     * 从[index]位置起，插入数据列表[elements]
     */
    fun add(index: Int, elements: Collection<D>)

    fun remove(element: D)
    fun remove(index: Int)

    /**
     * 从[startIndex]开始删除[size]个元素
     */
    fun remove(startIndex: Int, size: Int)

    /**
     * 删除与[element]相同的元素:相同取决于元素的equal实现
     * [element]不必连续，如果连续，优先使用[remove]的带索引的方法
     */
    fun remove(element: Collection<D?>)

    /**
     * 如果[index]在数据范围内，则更新[index]位置的数据为[element]
     *
     * @see updateOrThrow
     */
    fun update(index: Int, element: D, payload: Any? = null)

    /**
     * 从[start]到[end]更新[payload]
     */
    fun update(start: Int, end: Int = itemSize, payload: Any?)

    /**
     * 更新[pos]的[payload]
     */
    fun update(pos: Int, payload: Any?) = update(pos, pos + 1, payload)

    /**
     * 更新[index]位置的数据为[element]，如果[index]超出数据范围，则抛出异常
     *
     * @see update
     */
    fun updateOrThrow(index: Int, element: D, payload: Any? = null)

    /**
     * 如果在原有数据长度内，则从[startIndex]开始，更改[elements]长度的元素为[elements]
     *
     * @see updateOrThrow
     */
    fun update(startIndex: Int, elements: Collection<D>, payload: Any? = null)

    /**
     * 从[startIndex]开始，更改[elements]长度的元素，如果[elements]元素超出原有数据长度，则会抛出异常
     *
     * @see update
     */
    fun updateOrThrow(startIndex: Int, elements: Collection<D>, payload: Any? = null)

    /**
     * 如果能获取到[index]的值，则返回该值，否则返回null，不会抛出异常
     */
    fun get(index: Int): D?
    fun getIndex(element: D): Int?
    fun contains(element: D): Boolean
}

interface DataHandler<D> {
    fun handle(data: MutableList<D>): MutableList<D> {
        return data.filter { handle(it) != null }.toMutableList()
    }

    fun handle(data: D): D?
}