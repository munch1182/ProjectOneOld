package com.munch.pre.lib.helper.data

/**
 * 作为效数据存储，应该实现的方法
 *
 * 除了这几个操作方法，还应该提供简短的默认实例方法，以及多类型区分的实例方法
 *
 * 放入的时候可以放入null值，
 * 但是取出的时候必须传入不为null的默认值，因此如果没有这个值或者取出的值为空，则直接返回默认值，因此取出的值不会为null
 * 如果没有默认值，则因为根据是否有key来判断，而不是直接取值
 *
 * Create by munch1182 on 2021/4/1 13:57.
 */
interface DataFunHelper<KEY> {

    /**
     * @see remove
     */
    fun put(key: KEY, value: Any?)

    /**
     * @see hasKey
     */
    fun <T> get(key: KEY, defValue: T): T

    fun remove(key: KEY): Boolean

    fun hasKey(key: KEY): Boolean

    fun clear()

}