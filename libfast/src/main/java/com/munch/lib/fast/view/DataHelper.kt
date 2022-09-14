package com.munch.lib.fast.view

import android.app.Activity
import com.munch.lib.android.extend.to
import com.munch.lib.android.helper.data.SPHelper
import kotlin.reflect.KClass

object DataHelper : SPHelper("libFast") {

    private const val KEY_FIRST_PAGE = "KEY_FIRST_PAGE"

    fun setFirstPage(target: KClass<out Activity>?) {
        if (target == null) {
            remove(KEY_FIRST_PAGE)
        } else {
            put(KEY_FIRST_PAGE, target.java.canonicalName)
        }
    }

    val firstPage: Class<in Activity>?
        get() = get<String>(KEY_FIRST_PAGE, null)?.let { Class.forName(it) }?.to()
}