package com.munch.lib.android.dialog

import androidx.activity.ComponentDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.android.extend.impInMain
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 给需要选择类的dialog提供选择的获取
 */
interface ChoseDialog : IDialog {

    /**
     * 用户对当前Dialog的选项所做的选择
     *
     * 只有当Dialog取消显示时才有值
     */
    val chose: IDialogChose?
}

interface IDialogChose {

    /**
     * 是否选择了取消
     */
    val isChoseCancel: Boolean

    /**
     * 是否选择了确认
     */
    val isChoseNext: Boolean
}

sealed class DialogChose : IDialogChose {

    object Next : DialogChose()
    object Cancel : DialogChose()

    override val isChoseCancel: Boolean
        get() = this == Cancel
    override val isChoseNext: Boolean
        get() = this == Next
}

//<editor-fold desc="extend">
/**
 * 显示[ChoseDialog]
 * 并当[ChoseDialog]消失时, 返回其选择的结果
 */
suspend fun ChoseDialog.showThenReturnChose(): IDialogChose? = suspendCancellableCoroutine {
    impInMain {
        this.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                owner.lifecycle.removeObserver(this)
                it.resume(this@showThenReturnChose.chose)
            }
        })
        show()
    }
}

abstract class ChoseDialogWrapper : ChoseDialog {

    protected open val dialog by lazy { crateDialog() }
    private var finChose: DialogChose? = null

    override val chose: IDialogChose?
        get() = finChose

    fun choseNext() {
        finChose = DialogChose.Next
    }

    fun choseCancel() {
        finChose = DialogChose.Cancel
    }

    protected abstract fun crateDialog(): ComponentDialog

    override fun show() {
        dialog.show()
    }

    override fun cancel() {
        dialog.cancel()
    }

    override fun getLifecycle(): Lifecycle = dialog.lifecycle

}
//</editor-fold>

