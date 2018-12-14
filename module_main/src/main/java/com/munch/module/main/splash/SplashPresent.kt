package com.munch.module.main.splash

/**
 * Created by Munch on 2018/12/8.
 */
class SplashPresent : Contract.Present {

    lateinit var m: Contract.Model
    override var v: Contract.View? = null

    override fun start() {
        m = SplashModel()
        v?.setNoticeText(m.getNoticeText())
    }
}