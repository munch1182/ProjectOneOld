package com.munch.pre.lib.helper.data

/**
 * 作为数据存储，应该实现的方法
 *
 * 除了这几个操作方法，还应该提供简短的默认实例方法，以及多类型区分的实例方法
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
     */
    fun put(key: KEY, value: Any?)

    fun remove(key: KEY): Boolean

    fun clear()

    fun update(key: KEY, defValue: Any?, updateValue: Any?) {
        if (hasKey(key)) {
            put(key, updateValue)
        } else {
            put(key, defValue)
        }
    }

    fun update(key: KEY, updateValue: Any?) = update(key, updateValue, updateValue)

    fun update(key: KEY, defValue: Any?, update: Any?.() -> Any) {
        if (hasKey(key)) {
            put(key, update.invoke(get(key, defValue)))
        } else {
            put(key, defValue)
        }
    }

    /**
     * @see hasKey
     */
    fun <T> get(key: KEY, defValue: T): T

    fun hasKey(key: KEY): Boolean

    @Suppress("UNCHECKED_CAST")
    fun <T : Number> plus(key: KEY, defValue: T, plusValue: T) {
        update(key, defValue) {
            when (this) {
                is Int -> this + plusValue.toInt()
                is Long -> this + plusValue.toLong()
                is Float -> this + plusValue.toFloat()
                is Double -> this + plusValue.toDouble()
                else -> throw UnsupportedOperationException()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Number> minus(key: KEY, defValue: T, minusValue: T) {
        update(key, defValue) {
            when (this) {
                is Int -> this - minusValue.toInt()
                is Long -> this - minusValue.toLong()
                is Float -> this - minusValue.toFloat()
                is Double -> this - minusValue.toDouble()
                else -> throw UnsupportedOperationException()
            }
        }
    }

    fun <T : Number> increment(key: KEY, defValue: T) = plus(key, defValue, 1)
    fun <T : Number> decrement(key: KEY, defValue: T) = minus(key, defValue, 1)

    fun toggle(key: KEY, defValue: Boolean) {
        update(key, defValue) {
            if (this is Boolean) {
                return@update !this
            }
            throw UnsupportedOperationException()
        }
    }

}
