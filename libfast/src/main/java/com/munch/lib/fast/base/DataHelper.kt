package com.munch.lib.fast.base

import com.munch.lib.helper.data.DataFun
import com.munch.lib.helper.data.MMKVHelper

/**
 * Create by munch1182 on 2022/4/15 20:50.
 */
object DataHelper : DataFun<String> by MMKVHelper.default() {

    init {
        MMKVHelper.init()
    }
}