package com.munch.module.main.splash

import android.app.Activity
import com.munch.common.base.mvp.IPresenter
import com.munch.common.base.mvp.IView

/**
 * Created by Munch on 2018/12/8.
 */
interface Contract {

    interface Model {

        fun getNoticeText(): CharSequence
    }

    interface View : IView {

        fun setNoticeText(notice: CharSequence)

        fun next(clazz: Class<out Activity>)
    }

    interface Present : IPresenter<View>
}