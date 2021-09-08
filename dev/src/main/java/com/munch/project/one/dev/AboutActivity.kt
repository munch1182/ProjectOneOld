package com.munch.project.one.dev

import android.os.Bundle
import android.widget.TextView
import com.munch.lib.fast.FastAppHelper
import com.munch.lib.fast.base.BaseBigTextTitleActivity

/**
 * Create by munch1182 on 2021/8/10 15:08.
 */
class AboutActivity : BaseBigTextTitleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val sb = StringBuilder()
        FastAppHelper.collectPhoneInfo().forEach {
            sb.append("${it.key}:${it.value}\r\n")
        }
        sb.append("debuggable:${BuildConfig.DEBUG}\r\n")
            .append("version name:${BuildConfig.VERSION_NAME}\r\n")
            .append("version code:${BuildConfig.VERSION_CODE}\r\n")
            .append("build time:${BuildConfig.BUILD_TIME}")

        findViewById<TextView>(R.id.about_tv).text = sb.toString()
    }
}