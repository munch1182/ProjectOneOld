package com.munch.module.main.splash

import android.app.Activity
import android.os.Bundle
import com.munch.common.base.activity.BaseActivity
import com.munch.common.base.helper.ViewHelper
import com.munch.module.main.R
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * Created by Munch on 2018/12/8.
 */
class SplashActivity : BaseActivity(), Contract.View {

    override fun setNoticeText(notice: CharSequence) {
        tv_notice_main_splash.text = notice
    }

    override fun next(clazz: Class<out Activity>) {
    }

    override fun initView(bundle: Bundle?) {
        super.initView(bundle)
        setContentView(R.layout.activity_splash)
        ViewHelper.fullScreen(this)
    }

    override fun initData(bundle: Bundle?) {
        super.initData(bundle)
        SplashPresent().takeView(this).manageView().start()
    }

    override fun onDestroy() {
        super.onDestroy()
        ViewHelper.release(this)
    }
}
