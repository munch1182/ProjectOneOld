package com.munch.lib.android.helper.data

import android.content.Context
import android.content.SharedPreferences
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.toOrNull

open class SPHelper(private val sp: SharedPreferences) : DataFunHelper<String> {

    constructor(name: String) : this(AppHelper.getSharedPreferences(name, Context.MODE_PRIVATE))

    object Default : SPHelper("SPHelper")

    override suspend fun put(key: String, value: Any?) {
        sp.edit().apply { putVal(key, value, this) }.commit()
    }

    override suspend fun remove(key: String): Boolean {
        sp.edit().remove(key).commit()
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> get(key: String, defValue: T?): T? {
        if (!hasKey(key)) return null
        val obj: Any? = when (defValue) {
            is String? -> sp.getString(key, defValue?.toOrNull())
            is Int? -> sp.getInt(key, (defValue?.toOrNull()) ?: 0)
            is Boolean? -> sp.getBoolean(key, (defValue?.toOrNull()) ?: false)
            is Float? -> sp.getFloat(key, (defValue?.toOrNull()) ?: 0f)
            is Long? -> sp.getLong(key, (defValue?.toOrNull()) ?: 0L)
            is MutableSet<*>? -> sp.getStringSet(key, defValue?.toOrNull())
            else -> null
        }
        return obj as? T? ?: defValue
    }

    override suspend fun clear() {
        sp.edit().clear().commit()
    }

    override suspend fun hasKey(key: String) = sp.contains(key)

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

    override suspend fun toMap(): MutableMap<String, *> = sp.all
}