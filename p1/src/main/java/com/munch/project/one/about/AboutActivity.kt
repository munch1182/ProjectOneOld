package com.munch.project.one.about

import android.os.Bundle
import android.view.MenuItem
import com.munch.lib.extend.bind
import com.munch.lib.extend.icontext.putStr2Clip
import com.munch.lib.extend.shareText
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.base.PhoneHelper
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.ISupportShareActionBar
import com.munch.lib.fast.view.SupportConfigDialog
import com.munch.lib.fast.view.SupportShareActionBar
import com.munch.project.one.databinding.LayoutContentOnlyBinding

/**
 * Created by munch1182 on 2022/4/30 20:23.
 */
class AboutActivity : BaseFastActivity(),
    ActivityDispatch by (SupportShareActionBar + SupportConfigDialog()) {

    private val bind by bind<LayoutContentOnlyBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.content.apply {
            setLineSpacing(0f, 1.3f)
        }
        bind.content.text = StringBuilder().apply {
            PhoneHelper.collect().forEach {
                append(it.key).append(": ").append(it.value).append("\n")
            }

            append("\n")
            append(PhoneHelper.desc())
        }.toString()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (ISupportShareActionBar.isShare(item)) {
            val content = bind.content.text
            putStr2Clip(content)
            shareText(content)
            return true
        }
        return super<BaseFastActivity>.onOptionsItemSelected(item)
    }
}