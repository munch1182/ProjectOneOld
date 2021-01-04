package com.munch.lib.helper

import android.app.Application
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import java.util.*


/**
 * 语言切换帮助基类
 *
 * 是abstract的原因是需要自行实现持久化
 *
 * 使用时:先实现然后调用[registerApp]并在每个activity的[android.app.Activity.attachBaseContext]中调用[attachBaseContent]包裹参数context
 *
 * 切换时调用[switchLanguage]，但注意，语言切换需要重建activity，可以使用[android.app.Activity.recreate]或者新打开一个activity，已打开的activity也需要重新打开
 *
 *
 * Create by munch1182 on 2020/12/30 10:23.
 */
abstract class LanguageBaseHelper {

    private var languageData = ""
    private var followSystem = true

    /**
     * 使用时应该缓存
     */
    abstract fun saveLanguageSet(language: String, followSystem: Boolean)

    abstract fun queryLanguageSet(systemLanguage: String): Pair<String, Boolean>

    /**
     * 需要被调用才能生效
     *
     * @see android.app.Activity.attachBaseContext
     */
    open fun attachBaseContent(context: Context): Context {
        return context.createConfigurationContext(context.resources.configuration.apply {
            setLocale(
                Locale(languageData, Locale.getDefault().country, Locale.getDefault().variant)
            )
        })
    }

    fun getNowLanguage() = languageData

    /**
     * 此方法只是切换语言配置
     * 要使其生效，得[attachBaseContent]被调用
     * 即activity需要重新生成 [Activity.recreate()]
     */
    open fun switchLanguage(language: String) {
        languageData = language
        saveLanguageSet(language, followSystem)
    }

    /**
     * 需要在app中注册来获取初始状态
     */
    open fun registerApp(application: Application) {
        initLanguage(Locale.getDefault().language)
        //无论是否跟随系统变化都注册回调，是为了适应followSystem的动态更改
        application.registerComponentCallbacks(object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                onSystemChanged(newConfig)
            }

            override fun onLowMemory() {
            }
        })
    }

    /**
     * 初始化语言，此处应该从持久化中获取
     *
     * @see queryLanguageSet
     */
    protected open fun initLanguage(systemLanguage: String) {
        val (language, followSystem) = queryLanguageSet(systemLanguage)
        languageData = if (followSystem) {
            systemLanguage
        } else {
            language
        }
    }

    /**
     * 此处应该持久化，且同步[followSystem]的值
     */
    open fun followSystem(follow: Boolean = true): LanguageBaseHelper {
        this.followSystem = follow
        saveLanguageSet(languageData, follow)
        return this
    }


    /**
     * 系统语言变化回调
     */
    protected fun onSystemChanged(newConfig: Configuration) {
        if (!followSystem) {
            return
        }
        val language = Locale.getDefault().language
        if (languageData != language) {
            saveLanguageSet(language, followSystem)
            languageData = language
        }
    }

}