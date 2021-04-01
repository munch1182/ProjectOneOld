package com.munch.pre.lib.helper.data

/**
 * 作为效数据存储，应该实现的方法
 *
 * 除了这几个操作方法，还应该提供简短的默认实例方法，以及多类型区分的实例方法
 *
 * Create by munch1182 on 2021/4/1 13:57.
 */
interface DataFunHelper<KEY> {

    /**
     * @see remove
     */
    fun put(key: KEY, value: Any)

    /**
     * @see hasKey
     */
    fun <T> get(key: KEY, defValue: T): T

    fun remove(key: KEY): Boolean

    fun hasKey(key: KEY): Boolean

    fun clear()

}