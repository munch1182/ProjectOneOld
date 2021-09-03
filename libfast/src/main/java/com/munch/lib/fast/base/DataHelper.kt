package com.munch.lib.fast.base

import com.munch.lib.helper.data.MMKVHelper

/**
 * Create by munch1182 on 2021/8/14 11:27.
 */
object DataHelper {

    var selectedActivity: Class<out BaseActivity>?
        set(value) {
            SET.instance.put(SET.KEY_SELECTED_ACTIVITY, value?.canonicalName)
        }
        @Suppress("UNCHECKED_CAST")
        get() = SET.instance.get<String>(SET.KEY_SELECTED_ACTIVITY, null)?.let {
            try {
                Class.forName(it) as? Class<BaseActivity>
            } catch (e: Exception) {
                null
            }
        }

    var keepLightOn: Boolean
        set(value) {
            SET.instance.put(SET.KEY_KEEP_LIGHT_ON, value)
        }
        get() = SET.instance.get(SET.KEY_KEEP_LIGHT_ON, false)!!

    private object SET {

        private const val ID_KEY_SET = "id_key_set"

        const val KEY_SELECTED_ACTIVITY = "key_selected_activity"
        const val KEY_KEEP_LIGHT_ON = "key_keep_light_on"

        val instance by lazy { MMKVHelper(ID_KEY_SET) }
    }

    object App {

        private const val ID_KEY_APP = "id_key_app"

        val instance by lazy { MMKVHelper(ID_KEY_APP) }
    }
}