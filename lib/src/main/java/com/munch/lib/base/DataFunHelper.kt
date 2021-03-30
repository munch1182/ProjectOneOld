package com.munch.lib.base

/**
 * Create by munch1182 on 2021/3/30 10:22.
 */
interface DataFunHelper<KEY> {

    /**
     * @see remove
     */
    fun put(key: KEY, value: Any)

    /**
     * 必须要传入一个默认值，如果没有此数据或者数据为null，则会返回默认值
     *
     * @see hasKey
     */
    fun <T> get(key: KEY, defValue: T): T

    fun remove(key: KEY): Boolean

    fun hasKey(key: KEY): Boolean

    fun clear()

}