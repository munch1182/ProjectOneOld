package com.munch.lib.android.dialog

import androidx.lifecycle.DefaultLifecycleObserver
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
    val chose: DialogChose?
}

interface DialogChose {

    /**
     * 是否选择了取消
     */
    val isChoseCancel: Boolean

    /**
     * 是否选择了确认
     */
    val isChoseNext: Boolean
}

//<editor-fold desc="extend">
/**
 * 显示[ChoseDialog]
 * 并当[ChoseDialog]消失时, 返回其选择的结果
 */
suspend fun ChoseDialog.showThenReturnChose(): DialogChose? = suspendCancellableCoroutine {
    this.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            owner.lifecycle.removeObserver(this)
            it.resume(this@showThenReturnChose.chose)
        }
    })
    impInMain { show() }
}
//</editor-fold>

