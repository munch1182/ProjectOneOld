package com.munch.project.launcher.base

import com.munch.lib.helper.MMKVHelper

/**
 * Create by munch1182 on 2021/3/30 11:39.
 */
class DataHelper private constructor(id: String) : MMKVHelper(id, multiProcess = true) {

    companion object {

        private const val ID_DEF = "id_for_mmkv_def"
        private const val ID_SET = "id_for_mmkv_set"

        val INSTANCE_DEF = DataHelper(ID_DEF)
        val INSTANCE_SET = DataHelper(ID_SET)
    }

}