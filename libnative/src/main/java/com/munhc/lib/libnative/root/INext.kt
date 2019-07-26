package com.munhc.lib.libnative.root

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Created by Munch on 2019/7/13 13:59
 */
interface INext : IContext {

    companion object {
        fun startActivity(context: Context, target: Class<out Activity>, bundle: Bundle? = null) {
            context.startActivity(Intent(context, target).apply {
                bundle ?: return@apply
                putExtras(bundle)
            })
        }

    }

    fun startActivity(target: Class<out Activity>, bundle: Bundle? = null) {
        startActivity(getContext() ?: return, target, bundle)
    }

}