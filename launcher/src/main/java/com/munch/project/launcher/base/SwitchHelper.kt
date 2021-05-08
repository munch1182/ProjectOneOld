package com.munch.project.launcher.base

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.munch.pre.lib.base.LanguageBaseHelper
import com.munch.project.launcher.R
import java.util.*

/**
 * Create by munch1182 on 2021/4/8 14:55.
 */
class SwitchHelper : LanguageBaseHelper() {

    companion object {

        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_FOLLOW = "key_follow"
        private const val KEY_NIGHT = "key_night"
        private const val KEY_THEME = "key_theme"

        private const val THEME_DEF = 0
        private const val THEME_MODE_1 = 1

        val INSTANCE by lazy { SwitchHelper() }
    }

    private var cacheLanguage: String? = null
    private var cacheFollow = true
    private var cacheDayNightMode = 0
    private var cacheThemeMode = THEME_DEF

    fun switchNight(mode: Int = switchNightOrNot()) {
        if (cacheDayNightMode != mode) {
            cacheDayNightMode = mode
            DataHelper.SET.put(KEY_NIGHT, cacheDayNightMode)
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    fun switchTheme(themeMode: Int = if (getThemeMode() == THEME_MODE_1) THEME_DEF else THEME_MODE_1) {
        cacheThemeMode = themeMode
        DataHelper.SET.put(KEY_THEME, cacheThemeMode)
    }

    fun switchLanguage() {
        super.switchLanguage(if (getNowLanguage() == "zh") "en" else "zh")
    }

    private fun switchNightOrNot() =
        if (isNightMode()) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES

    fun isNightMode() = getNightMode() == AppCompatDelegate.MODE_NIGHT_YES

    fun getNightMode() = cacheDayNightMode

    fun getThemeMode() = cacheThemeMode

    override fun registerApp(application: Application) {
        super.registerApp(application)
        cacheDayNightMode =
            DataHelper.SET.get(KEY_NIGHT, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        switchNight(cacheDayNightMode)
        cacheThemeMode = DataHelper.SET.get(KEY_THEME, THEME_DEF)
    }

    override fun saveLanguageSet(language: String, followSystem: Boolean) {
        DataHelper.SET.put(KEY_LANGUAGE, language)
        DataHelper.SET.put(KEY_FOLLOW, followSystem)
    }

    override fun queryLanguageSet(systemLanguage: String): Pair<String, Boolean> {
        cacheLanguage = DataHelper.SET.get(KEY_LANGUAGE, systemLanguage)
        cacheFollow = DataHelper.SET.get(KEY_FOLLOW, true)
        return Pair(cacheLanguage!!, cacheFollow)
    }

    fun switchTheme(activity: AppCompatActivity) {
        activity.setTheme(getThemeByMode())
    }

    private fun getThemeByMode(): Int {
        return when (getThemeMode()) {
            THEME_DEF -> R.style.AppTheme
            else -> R.style.AppTheme_Skin1
        }
    }

    fun getTestSuffix(): String {
        return if (getNowLanguage().toLowerCase(Locale.ROOT) == "zh") "" else "_en"
    }

}