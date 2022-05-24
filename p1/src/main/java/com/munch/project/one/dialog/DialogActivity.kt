package com.munch.project.one.dialog

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.munch.lib.OnCancel
import com.munch.lib.Priority
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.fvFv
import com.munch.lib.fast.view.supportDef
import com.munch.lib.helper.ARSHelper
import com.munch.lib.notice.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Created by munch1182 on 2022/5/17 22:05.
 */
class DialogActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val bind by fvFv(arrayOf("start"))
    private val anm = ActivityNoticeManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.click { _, index ->
            when (index) {
                0 -> startDialog()
            }
        }
    }

    private fun startDialog() {
        lifecycleScope.launch {
            withContext(Dispatchers.Default) { delay(Random.nextLong(3 * 1000L)) }
            AlertDialog
                .Builder(this@DialogActivity)
                .setMessage("权限更改，需要重新请求")
                .toNotice()
                .byManager(anm)
                .show()
        }
        lifecycleScope.launch {
            withContext(Dispatchers.Default) { delay(Random.nextLong(3 * 1000L)) }
            AlertDialog
                .Builder(this@DialogActivity)
                .setMessage("有新版本可以更新")
                .toNotice()
                .byManager(anm)
                .show()
        }
        lifecycleScope.launch {
            withContext(Dispatchers.Default) { delay(Random.nextLong(3 * 1000L)) }
            AlertDialog
                .Builder(this@DialogActivity)
                .setMessage("您有新的消息需要处理")
                .toNotice(Priority(99))
                .byManager(anm)
                .show()
        }
        lifecycleScope.launch {
            withContext(Dispatchers.Default) { delay(Random.nextLong(3 * 1000L)) }
            AlertDialog
                .Builder(this@DialogActivity)
                .setMessage("您的账号已过期或者已在其它地方登录")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    anm.removeOthers()
                    anm.cancel()
                }
                .toNotice(Priority(100))
                .byManager(anm)
                .show()
        }
        lifecycleScope.launch {
            withContext(Dispatchers.Default) { delay(Random.nextLong(3 * 1000L)) }
            AlertDialog
                .Builder(this@DialogActivity)
                .setMessage("您的设备断开连接")
                .toNotice()
                .byManager(anm)
                .show()
        }
    }
}


class DialogNotice(
    private val dialog: AlertDialog,
    override val priority: Priority = Priority(0)
) : Notice {

    private val onSelect = ARSHelper<OnSelect>()
    private var onCancel: OnCancel? = null

    init {
        val function: (dialog: DialogInterface, which: Int) -> Unit =
            { _, which -> onSelect.notifyUpdate { it.invoke(Chose(which)) } }
        dialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            dialog.context.getString(android.R.string.ok), function
        )
        dialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            dialog.context.getString(android.R.string.ok), function
        )
        dialog.setOnCancelListener {
            it.cancel()
            onSelect.clear()
            onCancel?.invoke()
        }
    }

    override fun addOnCancel(onCancel: OnCancel?): DialogNotice {
        this.onCancel = onCancel
        return this
    }

    override fun show() {
        if (isShowing) {
            return
        }
        dialog.show()
    }

    override fun cancel() {
        dialog.cancel()
    }

    override fun addOnSelect(chose: OnSelect): DialogNotice {
        onSelect.add(chose)
        return this
    }

    override val isShowing: Boolean
        get() = dialog.isShowing
}

@Suppress("NOTHING_TO_INLINE")
inline fun AlertDialog.Builder.toNotice(priority: Priority = Priority(0)) =
    DialogNotice(this.create(), priority)