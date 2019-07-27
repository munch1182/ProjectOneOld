package com.munhc.lib.libnative.helper

import android.content.Context
import android.content.SharedPreferences
import com.munhc.lib.libnative.excetion.MethodEcxception

/**
 * Created by Munch on 2019/7/26 14:37
 */
object SpHelper {

    private const val SP_NAME = "SP_NAME"
    private var sSharedPreferences: SharedPreferences? = null
    /**
     * 缓存的当前使用过的sp的名称
     */
    private var spName: String? = null

    @JvmStatic
    fun put(key: String, any: Any?) {
        val edit = getSp().edit()
        putVal(key, any, edit)
        edit.apply()
    }

    @JvmStatic
    fun remove(key: String) {
        getSp().edit().remove(key).apply()
    }

    @JvmStatic
    fun hasKey(key: String): Boolean {
        return getSp().contains(key)
    }

    /**
     * 同步调用，移除并在移除成功或失败之后再继续执行
     */
    @JvmStatic
    fun remove2knowEnd(key: String): Boolean {
        return getSp().edit().remove(key).commit()
    }

    @JvmStatic
    fun clear() {
        getSp().edit().clear().apply()
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> getVal(key: String, defVal: T?): T? {
        val obj: Any? = when (defVal) {
            is String -> getSp().getString(key, defVal as String)
            is Int -> getSp().getInt(key, defVal as Int)
            is Boolean -> getSp().getBoolean(key, defVal as Boolean)
            is Float -> getSp().getFloat(key, defVal as Float)
            is Long -> getSp().getLong(key, defVal as Long)
            is MutableSet<*> -> getSp().getStringSet(key, defVal as MutableSet<String>)
            else -> null
        }
        return obj as T?
    }

    private fun putVal(key: String, any: Any?, edit: SharedPreferences.Editor) {
        @Suppress("UNCHECKED_CAST")
        when (any) {
            is Int -> edit.putInt(key, any)
            is Float -> edit.putFloat(key, any)
            is Boolean -> edit.putBoolean(key, any)
            is Long -> edit.putLong(key, any)
            is String -> edit.putString(key, any)
            is MutableSet<*> -> edit.putStringSet(key, any as MutableSet<String>)
            else -> throw MethodEcxception.unSupport()
        }
    }

    private fun getName(): String {
        return SP_NAME
    }

    private fun getContext(): Context {
        return AppHelper.getContext()
    }

    private fun getSp(): SharedPreferences {
        return sSharedPreferences ?: switchSp(AppHelper.getContext(), SP_NAME).sSharedPreferences!!
    }

    /**
     * 根据传入的[name]不同，可以切换不同的SharedPreferences对象
     */
    @JvmStatic
    @JvmOverloads
    fun switchSp(context: Context = AppHelper.getContext(), name: String): SpHelper {
        if (name != spName) {
            sSharedPreferences = null
        }
        if (sSharedPreferences == null) {
            sSharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        }
        return this
    }
}