package com.munch.lib.android.helper.data

/**
 * 作为数据存储，应该实现的方法
 *
 * 除了这几个操作方法，还应该提供简短的默认实例方法，以及多类型区分的实例方法，但这个应该自行实例化
 *
 * 放入的时候可以放入null值，
 * 但是取出的时候必须传入不为null的默认值，因此如果没有这个值或者取出的值为空，则直接返回默认值，因此取出的值不会为null
 * 如果没有默认值，则应该根据是否有key来判断，而不是直接取值
 *
 * Create by munch1182 on 2021/4/1 13:57.
 */
interface DataFunHelper<KEY> {

    /**
     * @see remove
     *
     * @param value 其是否能为null需要实现时自行差异化处理
     */
    suspend fun put(key: KEY, value: Any?)

    /**
     * 删掉[key]及其值
     */
    suspend fun remove(key: KEY): Boolean

    /**
     * 获取[key]的值，如果值不存在，则返回[defValue]
     *
     * @param defValue 其是否能为null需要实现时自行差异化处理或者创建该类型的重载方法
     *
     * @see hasKey
     */
    suspend fun <T> get(key: KEY, defValue: T? = null): T?

    /**
     * 清除所有key
     */
    suspend fun clear()

    /**
     * 判断是否已经存过[key]
     */
    suspend fun hasKey(key: KEY): Boolean

    suspend fun toMap(): Map<KEY, Any?>? = null
}

/**
 * 更新一个值，如果有这个值(有这个[key])，将这个值更新为[updateValue]，否则，将这个值设置为[defValue]
 *
 * @see update
 */
suspend fun <KEY> DataFunHelper<KEY>.update(
    key: KEY,
    updateValue: Any?,
    defValue: Any? = updateValue
) {
    if (hasKey(key)) {
        put(key, updateValue)
    } else {
        put(key, defValue)
    }
}

/**
 * 通过获取的值更新一个值
 *
 * @param update 此函数的参数为该[key]的值，如果没有这个[key]则为[defValue]，返回值为要更新的值
 * @param defValue 如果该[key]不存在，[update]的参数将为此值
 *
 * @see plus
 */
suspend fun <KEY> DataFunHelper<KEY>.update(
    key: KEY,
    update: (Any?) -> Any?,
    defValue: Any? = null
) {
    put(key, update.invoke(get(key, defValue)))
}

/**
 * 批量存储
 *
 * 默认实现是循环存储，如果有更高效方法的应自行实现
 */
suspend fun <KEY> DataFunHelper<KEY>.put(vararg pair: Pair<KEY, Any?>) {
    pair.forEach { put(it.first, it.second) }
}

/**
 * [Number]类型的[key]的值更新为加上[plusValue]的结果
 *
 * 如果[key]的值和[defValue]都为null，则会抛出异常
 */
suspend fun <KEY, T : Number> DataFunHelper<KEY>.plus(key: KEY, plusValue: T, defValue: T? = null) {
    update(key, {
        when (it) {
            is Byte -> it + plusValue.toByte()
            is Short -> it + plusValue.toShort()
            is Int -> it + plusValue.toInt()
            is Long -> it + plusValue.toLong()
            is Float -> it + plusValue.toFloat()
            is Double -> it + plusValue.toDouble()
            else -> throw UnsupportedOperationException()
        }
    }, defValue)
}

/**
 * [Number]类型的[key]的值更新为乘以[multiValue]的结果
 *
 * 如果[key]的值和[defValue]都为null，则会抛出异常
 */
suspend fun <KEY, T : Number> DataFunHelper<KEY>.times(
    key: KEY,
    multiValue: T,
    defValue: T? = null
) {
    update(key, {
        when (it) {
            is Byte -> it * multiValue.toByte()
            is Short -> it * multiValue.toShort()
            is Int -> it * multiValue.toInt()
            is Long -> it * multiValue.toLong()
            is Float -> it * multiValue.toFloat()
            is Double -> it * multiValue.toDouble()
            else -> throw UnsupportedOperationException()
        }
    }, defValue)
}

/**
 * 将[Boolean]类型的[key]的值更新为其相反的状态
 *
 * 如果[key]的值和[defValue]都为null，则会抛出异常
 */
suspend fun <KEY> DataFunHelper<KEY>.toggle(key: KEY, defValue: Boolean? = null) {
    update(key, {
        if (it !is Boolean) {
            throw UnsupportedOperationException()
        }
        return@update !it
    }, defValue)
}