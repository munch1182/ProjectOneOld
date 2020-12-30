package com.munch.project.test.switch

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.munch.lib.helper.LanguageBaseHelper
import com.munch.lib.helper.SpHelper

/**
 * Create by munch1182 on 2020/12/30 11:37.
 */
class SwitchHelper private constructor() : LanguageBaseHelper() {

    companion object {
        val INSTANCE by lazy { SwitchHelper() }

        private const val LANGUAGE_KEY_LANGUAGE = "language_key_language"
        private const val LANGUAGE_KEY_FOLLOW_SYSTEM = "language_key_follow_system"
        private const val DAY_NIGHT_KEY_MODE = "day_night_key_mode"
        private const val THEME_KEY_MODE = "theme_key_mode"
    }

    private var cacheLanguage: String? = null
    private var cacheFollow: Boolean? = null
    private var cacheDayNightMode = 0
    private var cacheThemeMode = 0

    override fun registerApp(application: Application) {
        super.registerApp(application)
        cacheDayNightMode = SpHelper.getSp()
            .get(DAY_NIGHT_KEY_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)!!

        switchDayNight(cacheDayNightMode)

        cacheThemeMode = SpHelper.getSp().get(THEME_KEY_MODE, 0)!!
    }

    fun switchThemeMode(themeMode: Int = ThemeHelper.THEME_DEF) {
        cacheThemeMode = themeMode
        SpHelper.getSp().put(THEME_KEY_MODE, themeMode)
    }

    fun isNightMode() = getNightMode() == AppCompatDelegate.MODE_NIGHT_YES

    fun getNightMode() = cacheDayNightMode

    fun getThemeMode() = cacheThemeMode

    fun switchDayNight(mode: Int = switchNightOrNot()) {
        cacheDayNightMode = mode
        AppCompatDelegate.setDefaultNightMode(mode)
        SpHelper.getSp().put(DAY_NIGHT_KEY_MODE, mode)
    }

    private fun switchNightOrNot() =
        if (getNightMode() == AppCompatDelegate.MODE_NIGHT_YES) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES

    override fun saveLanguageSet(language: String, followSystem: Boolean) {
        if (language != cacheLanguage) {
            SpHelper.getSp().put(LANGUAGE_KEY_LANGUAGE, language)
        }
        if (followSystem != cacheFollow) {
            SpHelper.getSp().put(LANGUAGE_KEY_FOLLOW_SYSTEM, followSystem)
        }
    }

    override fun queryLanguageSet(systemLanguage: String): Pair<String, Boolean> {
        if (cacheLanguage == null || cacheFollow == null) {
            cacheLanguage = SpHelper.getSp().get(LANGUAGE_KEY_LANGUAGE, systemLanguage)!!
            cacheFollow = SpHelper.getSp().get(LANGUAGE_KEY_FOLLOW_SYSTEM, true)!!
        }
        return Pair(cacheLanguage!!, cacheFollow!!)
    }

}