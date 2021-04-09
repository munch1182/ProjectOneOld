package com.munch.test.project.one.base

import android.content.Context
import android.os.Bundle
import com.munch.test.project.one.switch.SwitchHelper
import com.munch.lib.fast.base.activity.BaseItemActivity as BIA
import com.munch.lib.fast.base.activity.BaseItemWithNoticeActivity as BIWNA
import com.munch.lib.fast.base.activity.BaseRvActivity as BRA
import com.munch.lib.fast.base.activity.BaseTopActivity as BTA

/**
 * Create by munch1182 on 2021/4/8 15:05.
 */
open class BaseTopActivity : BTA() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SwitchHelper.INSTANCE.attachBaseContent(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SwitchHelper.INSTANCE.switchTheme(this)
        super.onCreate(savedInstanceState)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle("$title${SwitchHelper.INSTANCE.getTestSuffix()}")
    }
}

abstract class BaseItemActivity : BIA() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SwitchHelper.INSTANCE.attachBaseContent(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SwitchHelper.INSTANCE.switchTheme(this)
        super.onCreate(savedInstanceState)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle("$title${SwitchHelper.INSTANCE.getTestSuffix()}")
    }
}

abstract class BaseItemWithNoticeActivity : BIWNA() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SwitchHelper.INSTANCE.attachBaseContent(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SwitchHelper.INSTANCE.switchTheme(this)
        super.onCreate(savedInstanceState)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle("$title${SwitchHelper.INSTANCE.getTestSuffix()}")
    }
}

abstract class BaseRvActivity : BRA() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SwitchHelper.INSTANCE.attachBaseContent(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SwitchHelper.INSTANCE.switchTheme(this)
        super.onCreate(savedInstanceState)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle("$title${SwitchHelper.INSTANCE.getTestSuffix()}")
    }
}