package com.munch.lib.common.component

import android.content.Context
import com.alibaba.android.arouter.facade.template.IProvider

/**
 * 测试跨模块调用
 * Create by munch1182 on 2021/1/7 11:51.
 */
interface ThemeProvider : IProvider {

    fun getTheme(): Int

    override fun init(context: Context?) {
    }
}