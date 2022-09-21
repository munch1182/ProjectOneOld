package com.munch.lib.fast.view.dialog

/**
 * Create by munch1182 on 2022/9/20 11:48.
 */
object DialogHelper {

    fun message() = MessageDialog()

    /**
     * 仅提示消息的dialog
     */
    fun message(s: String) = message().message(s)

    /**
     * 从底部弹窗dialog
     */
    fun bottom() = BottomActionDialog()
}