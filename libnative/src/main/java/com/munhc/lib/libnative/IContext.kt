package com.munhc.lib.libnative

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import java.lang.RuntimeException

/**
 * Created by Munch on 2019/7/13 14:00
 */
interface IContext {

    fun getContext(): Context? {
        return when {
            this is Activity -> this
            this is Fragment -> this.context
            else -> {
                throw RuntimeException("need to implement")
            }
        }
    }
}