package com.munch.lib.android.helper.data

import android.content.Context
import android.content.SharedPreferences
import com.munch.lib.android.AppHelper

/**
 * Created by Munch on 2019/7/26 14:37
 */
class SpHelper private constructor(private val sp: SharedPreferences) :
    DataFun<String> {

    companion object {

        private const val SP_NAME = "SP_NAME"

        @JvmStatic
        fun getName() = SP_NAME

        /**
         * 根据传入的[name]不同，可以切换不同的SharedPreferences对象
         *
         * 如果使用频繁，建议自行实现单例
         */
        @JvmStatic
        fun getSp(context: Context = AppHelper.app, name: String = getName()): SpHelper {
            return SpHelper(context.getSharedPreferences(name, Context.MODE_PRIVATE))
        }
    }


    override fun put(key: String, value: Any?) {
        sp.edit().apply { putVal(key, value, this) }.apply()
    }

    override fun put(vararg pair: Pair<String, Any?>) {
        sp.edit().apply { pair.forEach { putVal(it.first, it.second, this) } }.apply()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String, defValue: T?): T? {
        val obj: Any? = when (defValue) {
            is String? -> sp.getString(key, defValue as String?)
            is Int? -> sp.getInt(key, (defValue as Int?) ?: 0)
            is Boolean? -> sp.getBoolean(key, (defValue as Boolean?) ?: false)
            is Float? -> sp.getFloat(key, (defValue as Float?) ?: 0f)
            is Long? -> sp.getLong(key, (defValue as Long?) ?: 0L)
            is MutableSet<*>? -> sp.getStringSet(key, defValue as MutableSet<String>?)
            else -> null
        }
        return obj as? T? ?: defValue
    }

    /**
     * 如果一次存入的值太多，应该使用此方法避免反复提交
     */
    fun put(map: HashMap<String, Any>) {
        sp.edit().apply {
            for (e in map) {
                putVal(e.key, e.value, this)
            }
        }.apply()
    }

    override fun remove(key: String): Boolean {
        sp.edit().remove(key).apply()
        return true
    }

    override fun hasKey(key: String): Boolean {
        return sp.contains(key)
    }

    /**
     * 同步调用，移除并在移除成功或失败之后再继续执行
     */
    fun removeByCommit(key: String): Boolean {
        return sp.edit().remove(key).commit()
    }

    override fun clear() {
        sp.edit().clear().apply()
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
            else -> throw IllegalStateException("unsupported")
        }
    }
}