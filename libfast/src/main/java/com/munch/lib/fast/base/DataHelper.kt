package com.munch.lib.fast.base

import com.munch.lib.helper.data.DataFun
import com.munch.lib.helper.data.MMKVHelper

/**
 * Create by munch1182 on 2022/4/15 20:50.
 */
object DataHelper : DataFun<String> by MMKVHelper.default() {


    //<editor-fold desc="startup">
    private const val KEY_START_UP = "start_up"

    fun saveStartUp(clazz: String?) {
        put(KEY_START_UP, clazz)
    }

    val startUp: Class<*>?
        get() = get<String>(KEY_START_UP, null)
            ?.let { Class.forName(it) }
    //</editor-fold>

    private const val KEY_SKIN_PATH = "skin_path"

    fun saveSkinPath(path: String?) {
        put(KEY_SKIN_PATH, path)
    }

    val skinPath: String?
        get() = get<String>(KEY_SKIN_PATH, null)
}