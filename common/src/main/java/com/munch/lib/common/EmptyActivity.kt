package com.munch.lib.common

import android.os.Bundle
import android.widget.TextView
import com.munch.lib.test.TestBaseTopActivity

/**
 * Create by munch1182 on 2021/1/7 13:53.
 */
class EmptyActivity : TestBaseTopActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "模块边界，该业务不在此模块中，如需测试请打包时编译该模块"
        })
        title = "组件拦截"
    }
}