package com.munch.lib.fast.base

import com.munch.lib.fast.helper.MMKVHelper
import com.munch.lib.helper.data.DataFun

/**
 * Create by munch1182 on 2022/4/15 20:50.
 */
object DataHelper : DataFun<String> by MMKVHelper.default() {


    //<editor-fold desc="startUp">
    private const val KEY_START_UP = "start_up"

    fun saveStartUp(clazz: String?) {
        put(KEY_START_UP, clazz)
    }

    val startUp: Class<*>?
        get() = get<String>(KEY_START_UP, null)
            ?.let { Class.forName(it) }
    //</editor-fold>
}