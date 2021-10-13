package com.munch.project.one.about

import android.os.Bundle
import android.widget.TextView
import androidx.core.view.setPadding
import com.munch.lib.base.dp2Px
import com.munch.lib.fast.FastAppHelper
import com.munch.lib.fast.base.BaseBigTextTitleActivity

/**
 * Create by munch1182 on 2021/10/6 13:59.
 */
class AboutActivity : BaseBigTextTitleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            setPadding(dp2Px(16f).toInt())
            val sb = StringBuilder()
            FastAppHelper.collectPhoneInfo().forEach {
                sb.append(it.key)
                    .append(":")
                    .append(it.value)
                    .append("\n")
            }
            text = sb.toString()
        })
    }
}