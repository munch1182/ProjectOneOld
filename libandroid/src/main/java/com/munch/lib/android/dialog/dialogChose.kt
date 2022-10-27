package com.munch.lib.android.dialog

import com.munch.lib.android.extend.SealedClassToStringByName

/**
 * 给需要选择类的dialog提供选择的获取
 */
interface ChoseDialog : IDialog, IDialogChose {

    /**
     * 用户对当前Dialog的选项所做的选择
     *
     * 只有当Dialog取消显示时才有值
     */
    val chose: IDialogChose?

    override val isChoseCancel: Boolean
        get() = chose?.isChoseCancel ?: false

    override val isChoseOk: Boolean
        get() = chose?.isChoseOk ?: false

    override val isChoseOther: Any?
        get() = chose?.isChoseOther

}

interface IDialogChose {

    /**
     * 是否选择了取消
     */
    val isChoseCancel: Boolean

    /**
     * 是否选择了确认
     */
    val isChoseOk: Boolean

    /**
     * 是否选择了其它
     */
    val isChoseOther: Any?
}

sealed class DialogChose : SealedClassToStringByName(), IDialogChose {

    object Ok : DialogChose()
    object Cancel : DialogChose()

    override val isChoseCancel: Boolean
        get() = this == Cancel
    override val isChoseOk: Boolean
        get() = this == Ok
    override val isChoseOther: Any? = null
}

