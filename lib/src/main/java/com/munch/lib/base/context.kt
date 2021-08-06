package com.munch.lib.base

import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Create by munch1182 on 2021/8/6 17:20.
 */

fun Context.startActivity(target: Class<*>, bundle: Bundle? = null) =
    startActivity(Intent(this, target).apply {
        val extras = bundle ?: return@apply
        putExtras(extras)
    })