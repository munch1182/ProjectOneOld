package com.munch.lib.dialog

import com.munch.lib.base.OnCancel
import com.munch.lib.base.OnNext

/**
 * Create by munch1182 on 2021/8/20 17:47.
 */
interface IDialog {

    fun show()

    fun dismiss()
}

/**
 * 让外部可以参与dialog的取消和确定方法回调
 */
interface IDialogHandler : IDialog {

    fun setOnCancel(onCancel: OnCancel): IDialogHandler

    fun setOnNext(onNext: OnNext): IDialogHandler
}