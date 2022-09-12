package com.munch.lib.android.dialog

interface DialogInterface {

    val isShowing: Boolean

    /**
     * 显示方法
     */
    fun show()

    /**
     * 取消方法
     *
     * 取消时必须调用此方法, 因此此方法可以看作取消显示的回调
     */
    fun cancel()
}