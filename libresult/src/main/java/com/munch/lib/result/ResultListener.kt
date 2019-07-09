package com.munch.lib.result

import android.content.Intent

/**
 * Created by Munch on 2019/7/9 10:58
 */
interface ResultListener {

    fun result(resultCode: Int, intent: Intent)
}