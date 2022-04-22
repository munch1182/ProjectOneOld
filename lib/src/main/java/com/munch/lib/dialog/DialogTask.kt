package com.munch.lib.dialog

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.munch.lib.task.ITask
import com.munch.lib.task.Key
import com.munch.lib.task.OrderTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

/**
 * Create by munch1182 on 2022/4/22 17:07.
 */
class DialogTask(private val wrapper: DialogWrapper) : OrderTask {

    constructor(dialog: AlertDialog) : this(AlertDialogWrapper(dialog))
    constructor(dialog: DialogFragment) : this(DialogFragmentWrapper(dialog))

    companion object {
        private val KEY_DIALOG = Key(9999)
    }

    override val coroutines: CoroutineContext
        get() = Dispatchers.Default

    override suspend fun run() {
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Any?> {
                wrapper
                    .setOnCancelListener { _ -> it.resume(true) }
                    .show()
            }
        }
    }


    override val orderKey: Key = KEY_DIALOG

}