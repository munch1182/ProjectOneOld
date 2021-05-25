package com.munch.project.launcher.base

import com.munch.pre.lib.base.BaseApp
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