package com.munch.test.lib.pre.base

import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.helper.data.MMKVHelper

/**
 * Create by munch1182 on 2021/4/1 14:03.
 */
object DataHelper {

    private const val ID_SET = "MMKV_ID_SET"

    val DEFAULT = MMKVHelper.default()

    val SET = MMKVHelper(ID_SET)

    fun init() = MMKVHelper.init(BaseApp.getInstance())
}