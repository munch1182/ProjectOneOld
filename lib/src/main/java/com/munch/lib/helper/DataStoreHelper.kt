package com.munch.lib.helper

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.createDataStore
import com.munch.lib.BaseApp
import com.munch.lib.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

/**
 * Create by munch1182 on 2021/3/23 15:33.
 */
class DataStoreHelper constructor(private var dataStore: DataStore<Preferences>) {

    companion object {
        private const val NAME_DATA_STORE_DEF = "data_store"

        fun def(
            context: Context = BaseApp.getContext(),
            name: String = NAME_DATA_STORE_DEF
        ): DataStoreHelper {
            return DataStoreHelper(context.createDataStore(name))
        }
    }

    fun <T> putInBlock(key: String, value: T) {
        runBlocking { put(key, value) }
    }

    fun <T> putInIO(key: String, value: T) {
        runBlocking(Dispatchers.IO) { put(key, value) }
    }

    fun <T> putInIO(map: HashMap<String, T>) {
        runBlocking(Dispatchers.IO) { put(map) }
    }

    suspend fun <T> put(map: HashMap<String, T>) {
        log(1)
        dataStore.edit { p ->
            map.keys.forEach {
                val value = map[it]
                p[getKeyByType(value, it)] = value
            }
        }
    }

    suspend fun <T> put(key: String, value: T) {
        dataStore.edit { it[getKeyByType(value, key)] = value }
    }

    fun <T> getInBlock(key: String, defValue: T): T {
        return runBlocking { get(key, defValue).firstOrNull() ?: defValue }
    }

    fun <T> get(key: String, defValue: T): Flow<T> {
        return dataStore.data.map { it[getKeyByType(defValue, key)] ?: defValue }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getKeyByType(value: T, key: String): Preferences.Key<T> {
        return when (value) {
            is Int -> intPreferencesKey(key)
            is Long -> longPreferencesKey(key)
            is String -> stringPreferencesKey(key)
            is Double -> doublePreferencesKey(key)
            is Float -> floatPreferencesKey(key)
            is Boolean -> booleanPreferencesKey(key)
            is Set<*> -> stringSetPreferencesKey(key)
            else -> throw UnsupportedOperationException("unsupported type")
        } as Preferences.Key<T>
    }
}