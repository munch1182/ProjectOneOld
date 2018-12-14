package com.munch.common.base.mvp

import android.content.Context

/**
 * Created by Munch on 2018/12/8.
 */
interface IView {

    /**
     * 如果非Context的子类须重写此方法
     */
    fun getContext(): Context {
        return this as Context
    }
}