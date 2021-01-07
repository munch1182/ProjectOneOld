package com.munch.project.test.component

import com.alibaba.android.arouter.facade.annotation.Route
import com.munch.lib.common.RouterHelper
import com.munch.lib.common.component.ThemeProvider
import com.munch.project.test.switch.SwitchHelper

/**
 * Create by munch1182 on 2021/1/7 11:55.
 */
@Route(path = RouterHelper.Test.PROVIDE_THEME)
class ThemeProviderImp : ThemeProvider {

    override fun getTheme(): Int {
        return SwitchHelper.INSTANCE.getThemeMode()
    }
}