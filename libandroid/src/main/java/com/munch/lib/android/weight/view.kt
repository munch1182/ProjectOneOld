package com.munch.lib.android.weight

import android.view.View
import com.munch.lib.android.extend.to

/**
 * Create by munch on 2022/9/18 8:58.
 */
interface ViewUpdate<V : View> {

    /**
     * 统一设置参数后, 在调用[View.requestLayout]或者[View.invalidate]等更新方法
     */
    fun update(update: V.() -> Unit) {
        update.invoke(this.to())
        this.to<View>().invalidate()
    }
}