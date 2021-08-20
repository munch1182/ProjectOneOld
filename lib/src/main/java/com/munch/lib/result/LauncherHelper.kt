package com.munch.lib.result

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat

/**
 * 将[ActivityResultCallback]移到[launch]方法中
 *
 * Create by munch1182 on 2021/8/20 14:32.
 */
class LauncherHelper<I, O>(caller: ActivityResultCaller, contract: ActivityResultContract<I, O>) {
    private var callback: ActivityResultCallback<O>? = null
    private val launcher: ActivityResultLauncher<I> =
        caller.registerForActivityResult(contract) {
            callback?.onActivityResult(it)
            callback = null
        }

    fun launch(input: I? = null, callback: ActivityResultCallback<O>) =
        launch(input, null, callback)

    fun launch(
        input: I? = null,
        options: ActivityOptionsCompat?,
        callback: ActivityResultCallback<O>
    ) {
        this.callback = callback
        launcher.launch(input, options)
    }
}

fun <I, O> ActivityResultCaller.register(contract: ActivityResultContract<I, O>): LauncherHelper<I, O> {
    return LauncherHelper(this, contract)
}