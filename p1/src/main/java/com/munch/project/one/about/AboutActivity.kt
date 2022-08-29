package com.munch.project.one.about

import android.os.Bundle
import com.munch.lib.extend.bind
import com.munch.lib.extend.getLanguage
import com.munch.lib.extend.getLocales
import com.munch.lib.extend.icontext.putStr2Clip
import com.munch.lib.extend.shareText
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.base.PhoneHelper
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef
import com.munch.project.one.BuildConfig
import com.munch.project.one.databinding.LayoutContentOnlyBinding

/**
 * Created by munch1182 on 2022/4/30 20:23.
 */
class AboutActivity : BaseFastActivity(),
    ActivityDispatch by supportDef() {

    private val bind by bind<LayoutContentOnlyBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addRightText("SHARE") { share() }
        bind.content.apply {
            setLineSpacing(0f, 1.3f)
        }
        bind.content.text = StringBuilder().apply {
            PhoneHelper.collect().forEach {
                append(it.key).append(": ").append(it.value).append("\n")
            }

            append("\n")
            append(PhoneHelper.desc())
            append("\n")

            append("\n")
            append("locales: ")
            append(getLocales().let {
                val sb = StringBuffer()
                for (i in 0 until it.size()) {
                    if (i > 0) sb.append(", ")
                    sb.append(it.get(i))
                }
                sb.toString()
            })
            append("\n")
            append("language: ")
            append(getLanguage())

            append("\n")
            append("\n")
            append(BuildConfig.VERSION_NAME)
            append("\n")
            append(BuildConfig.VERSION_CODE)
            append("\n")
            append(BuildConfig.BUILD_TIME)

        }.toString()
    }

    private fun share() {
        val content = bind.content.text
        putStr2Clip(content)
        shareText(content)
    }
}