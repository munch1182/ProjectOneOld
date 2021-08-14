package com.munch.lib.fast.base

import com.munch.lib.helper.data.MMKVHelper

/**
 * Create by munch1182 on 2021/8/14 11:27.
 */
object DataHelper {


    var firstActivity: Class<out BaseActivity>?
        set(value) {
            SET.instance.put(SET.KEY_FIRST_ACTIVITY, value?.canonicalName)
        }
        @Suppress("UNCHECKED_CAST")
        get() = SET.instance.get<String>(SET.KEY_FIRST_ACTIVITY, null)?.let {
            Class.forName(it) as? Class<BaseActivity>
        }

    private object SET {

        private const val ID_KEY_SET = "id_key_set"

        const val KEY_FIRST_ACTIVITY = "key_first_activity"

        val instance by lazy { MMKVHelper(ID_KEY_SET) }
    }
}