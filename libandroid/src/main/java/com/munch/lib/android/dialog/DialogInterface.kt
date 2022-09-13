package com.munch.lib.android.dialog

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.munch.lib.android.helper.ARSHelper

interface DialogInterface {

    val isShowing: Boolean

    /**
     * 显示方法
     */
    fun show()

    /**
     * 取消方法
     */
    fun cancel()
}

interface OnDialogLifecycle {
    fun onShow()
    fun onCancel()
}

interface OnDialogCreate {
    fun onCreateDialog(context: Context)
}

abstract class BaseDialog : DialogInterface, OnDialogCreate {

    protected open var life: ARSHelper<OnDialogLifecycle>? = null

    protected open var isShowingValue = false

    override val isShowing: Boolean
        get() = isShowingValue

    /**
     * 显示Dialog
     */
    override fun show() {
        isShowingValue = true
        life?.update { it.onShow() }
    }

    override fun cancel() {
        isShowingValue = false
        life?.update { it.onCancel() }
    }

    /**
     * 监听此Dialog的生命周期
     */
    fun addLifecycle(lifecycle: OnDialogLifecycle) {
        if (life != null) {
            life = ARSHelper()
        }
        life?.add(lifecycle)
    }

    fun removeLifecycle(lifecycle: OnDialogLifecycle) {
        life?.remove(lifecycle)
    }
}

abstract class BaseAlertDialog : BaseDialog() {

    protected open lateinit var dialog: AlertDialog

    override fun onCreateDialog(context: Context) {
        dialog = AlertDialog.Builder(context)
            .setView(onCreateView())
            .create()
    }

    abstract fun onCreateView(): View
}