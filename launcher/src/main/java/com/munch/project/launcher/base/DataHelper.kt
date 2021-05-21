package com.munch.project.launcher.base

import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.helper.data.DataFunHelper
import com.munch.pre.lib.helper.data.MMKVHelper
import com.munch.pre.lib.helper.data.SpHelper

/**
 * Create by munch1182 on 2021/5/8 11:15.
 */
object DataHelper {

    private const val ID_SET = "MMKV_ID_SET"
    private const val ID_TEST = "SP_ID_SET"

    val DEFAULT = MMKVHelper.default()

    val SET = MMKVHelper(ID_SET)

    fun init() = MMKVHelper.init(BaseApp.getInstance())

    fun new(id: String) = MMKVHelper(id)

    fun test() = SpHelper.getSp(name = ID_TEST)

    private const val KEY_SPAN_COUNT = "key_span_count"

    fun getSpanCount() = SET.get(KEY_SPAN_COUNT, 4)
    fun spanCount(count: Int) = SET.put(KEY_SPAN_COUNT, count)
}

@Suppress("UNCHECKED_CAST")
fun <T : Number> DataFunHelper<String>.plus(key: String, defValue: T, plusValue: T) {
    if (!hasKey(key)) {
        put(key, defValue)
    } else {
        val value = when {
            defValue is Int && plusValue is Int -> get(key, defValue) + plusValue
            defValue is Long && plusValue is Long -> get(key, defValue) + plusValue
            defValue is Float && plusValue is Float -> get(key, defValue) + plusValue
            defValue is Double && plusValue is Double -> get(key, defValue) + plusValue
            else -> throw UnsupportedOperationException()
        }
        put(key, value)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Number> DataFunHelper<String>.minus(key: String, defValue: T, minusValue: T) {
    if (!hasKey(key)) {
        put(key, defValue)
    } else {
        val value = when {
            defValue is Int && minusValue is Int -> get(key, defValue) - minusValue
            defValue is Long && minusValue is Long -> get(key, defValue) - minusValue
            defValue is Float && minusValue is Float -> get(key, defValue) - minusValue
            defValue is Double && minusValue is Double -> get(key, defValue) - minusValue
            else -> throw UnsupportedOperationException()
        }
        put(key, value)
    }
}

fun <T : Number> DataFunHelper<String>.increment(key: String, defValue: T) =
    when (defValue) {
        is Int -> plus(key, defValue, 1)
        is Long -> plus(key, defValue, 1L)
        is Float -> plus(key, defValue, 1f)
        is Double -> plus(key, defValue, 1.0)
        else -> throw UnsupportedOperationException()
    }

fun <T : Number> DataFunHelper<String>.decrement(key: String, defValue: T) =
    when (defValue) {
        is Int -> minus(key, defValue, 1)
        is Long -> minus(key, defValue, 1L)
        is Float -> minus(key, defValue, 1f)
        is Double -> minus(key, defValue, 1.0)
        else -> throw UnsupportedOperationException()
    }

fun DataFunHelper<String>.toggle(key: String, defValue: Boolean) {
    if (hasKey(key)) {
        put(key, !get(key, defValue))
    } else {
        put(key, defValue)
    }
}