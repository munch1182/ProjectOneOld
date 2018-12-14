package com.munch.module.main.splash

import com.munch.common.base.log.Loglog

/**
 * Created by Munch on 2018/12/8.
 */
class SplashModel : Contract.Model {

    private val startTime = 1544250917082

    override fun getNoticeText(): CharSequence {
        System.currentTimeMillis() - startTime
        return "233"
    }
}