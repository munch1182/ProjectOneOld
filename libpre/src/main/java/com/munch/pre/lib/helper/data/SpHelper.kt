@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.pre.lib.helper.data

import android.content.Context
import android.content.SharedPreferences
import com.munch.pre.lib.DefaultDepend
import com.munch.pre.lib.base.BaseApp

/**
 * Created by Munch on 2019/7/26 14:37
 */
class SpHelper private constructor(private var sharedPreferences: SharedPreferences) :
    DataFunHelper<String> {

    companion object {

        private const val SP_NAME = "SP_NAME"

        @JvmStatic
        fun getName() = SP_NAME


        @JvmStatic
        @DefaultDepend([BaseApp::class])
        fun getSp(context: Context = BaseApp.getInstance()) = getSp(context, SP_NAME)

        /**
         * 根据传入的[name]不同，可以切换不同的SharedPreferences对象
         */
        @JvmStatic
        @DefaultDepend([BaseApp::class])
        fun getSp(context: Context = BaseApp.getInstance(), name: String): SpHelper {
            return SpHelper(context.getSharedPreferences(name, Context.MODE_PRIVATE))
        }

    }

    private fun getSp() = sharedPreferences

    override fun put(key: String, value: Any) {
        getSp().edit().apply {
            putVal(key, value, this)
        }.apply()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String, defValue: T): T {
        val obj: Any? = when (defValue) {
            is String -> getSp().getString(key, defValue as String)
            is Int -> getSp().getInt(key, defValue as Int)
            is Boolean -> getSp().getBoolean(key, defValue as Boolean)
            is Float -> getSp().getFloat(key, defValue as Float)
            is Long -> getSp().getLong(key, defValue as Long)
            is MutableSet<*> -> getSp().getStringSet(key, defValue as MutableSet<String>)
            else -> null
        }
        return obj as? T? ?: defValue
    }

    /**
     * 如果一次存入的值太多，应该使用此方法避免反复提交
     */
    fun put(map: HashMap<String, Any>) {
        getSp().edit().apply {
            for (e in map) {
                putVal(e.key, e.value, this)
            }
        }.apply()
    }

    override fun remove(key: String): Boolean {
        getSp().edit().remove(key).apply()
        return true
    }

    override fun hasKey(key: String): Boolean {
        return getSp().contains(key)
    }

    /**
     * 同步调用，移除并在移除成功或失败之后再继续执行
     */
    fun remove2knowEnd(key: String): Boolean {
        return getSp().edit().remove(key).commit()
    }

    override fun clear() {
        getSp().edit().clear().apply()
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
            else -> throw Exception("unsupported")
        }
    }
}