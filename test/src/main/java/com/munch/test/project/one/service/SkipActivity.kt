package com.munch.test.project.one.service

import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import com.munch.pre.lib.log.log
import com.munch.test.project.one.base.BaseItemActivity

/**
 * Create by munch1182 on 2021/5/17 13:57.
 */
class SkipActivity : BaseItemActivity() {
    override fun clickItem(pos: Int) {
        if (pos == 0) {
            start()
        } else {
            stop()
        }
    }

    private fun start() {
        if (!isRunning()) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun isRunning(): Boolean {
        val enable: Int
        val resolver = applicationContext.contentResolver
        try {
            enable = Settings.Secure.getInt(resolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: Settings.SettingNotFoundException) {
            log(e)
            return false
        }
        if (enable == 1) {
            val value =
                Settings.Secure.getString(resolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                    ?: return false
            val splitter = TextUtils.SimpleStringSplitter(':')
            splitter.setString(value)
            while (splitter.hasNext()) {
                if (splitter.next().contains(SkipService::class.java.canonicalName!!)) {
                    return true
                }
            }
        }
        return false
    }

    private fun stop() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    override fun getItem(): MutableList<String> {
        return mutableListOf("start", "stop")
    }
}