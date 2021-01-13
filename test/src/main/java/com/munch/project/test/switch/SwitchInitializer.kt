package com.munch.project.test.switch

import android.content.Context
import androidx.startup.Initializer

/**
 * Create by munch1182 on 2021/1/13 17:11.
 */
class SwitchInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        //但是SwitchHelper内使用了Sp的对象，为此去初始化Sp更不合适
        /*SwitchHelper.INSTANCE.registerApp(context as Application)*/
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}