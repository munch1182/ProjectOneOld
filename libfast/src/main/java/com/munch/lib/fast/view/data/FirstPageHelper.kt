package com.munch.lib.fast.view.data

import android.app.Activity
import com.munch.lib.android.extend.to
import com.munch.lib.android.helper.data.SPHelper
import kotlin.reflect.KClass

object FirstPageHelper : SPHelper("libFirst") {

    private const val KEY_FIRST_PAGE = "KEY_FIRST_PAGE"

    suspend fun set(target: KClass<out Activity>?) {
        if (target == null) {
            remove(KEY_FIRST_PAGE)
        } else {
            put(KEY_FIRST_PAGE, target.java.canonicalName)
        }
    }

    suspend fun get(): Class<in Activity>? {
        return get(KEY_FIRST_PAGE, "")?.takeIf { it.isNotBlank() }?.let { Class.forName(it) }?.to()
    }
}