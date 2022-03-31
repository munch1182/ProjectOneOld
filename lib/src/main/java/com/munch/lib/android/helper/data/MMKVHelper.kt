package com.munch.lib.android.helper.data


import android.content.Context
import android.os.Parcelable
import com.tencent.mmkv.MMKV

/**
 * 不是单例因为MMKV的参数是可变的
 *
 * 同一时间存取多个数据应该使用同一个对象
 *
 * 可以根据业务情况自行实现并使用单例
 *
 * Create by munch1182 on 2021/3/30 9:59.
 *
 * @param id 区别存储的标识
 * @param multiProcess 在多进程下使用
 *
 * @see init 需要先初始化
 */
class MMKVHelper constructor(private val id: String = ID_DEF, multiProcess: Boolean = false) :
    DataFun<String> {

    private var processMode =
        if (multiProcess) MMKV.MULTI_PROCESS_MODE else MMKV.SINGLE_PROCESS_MODE
    private val instance by lazy { MMKV.mmkvWithID(id, processMode)!! }

    companion object {

        private const val ID_DEF = "MMKV_ID_DEF"

        fun init(context: Context) {
            MMKV.initialize(context)
        }

        fun default() = MMKVHelper()
    }

    @Suppress("UNCHECKED_CAST")
    override fun put(key: String, value: Any?) {
        if (value == null && hasKey(key)) {
            remove(key)
        } else {
            when (value) {
                is String -> instance.encode(key, value)
                is Int -> instance.encode(key, value)
                is Boolean -> instance.encode(key, value)
                is Float -> instance.encode(key, value)
                is Long -> instance.encode(key, value)
                is Double -> instance.encode(key, value)
                is ByteArray -> instance.encode(key, value)
                is Parcelable -> instance.encode(key, value)
                is MutableSet<*> -> instance.encode(key, value as MutableSet<String>?)
                else -> throw UnsupportedOperationException("unsupported type: ${value!!.javaClass}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String, defValue: T?): T? {
        return if (!hasKey(key)) {
            defValue
        } else {
            when (defValue) {
                is String? -> instance.decodeString(key, defValue as String?) as T?
                is Int? -> instance.decodeInt(key, (defValue as Int?) ?: 0) as T?
                is Boolean? -> instance.decodeBool(key, (defValue as Boolean?) ?: false) as T?
                is Float? -> instance.decodeFloat(key, (defValue as Float?) ?: 0f) as T?
                is Long? -> instance.decodeLong(key, (defValue as Long?) ?: 0L) as T?
                is Double? -> instance.decodeDouble(key, (defValue as Double?) ?: 0.0) as T?
                is ByteArray? -> instance.decodeBytes(key, defValue as ByteArray?) as T?
                is MutableSet<*>? -> instance.getStringSet(
                    key, defValue as MutableSet<String>
                ) as T?
                else -> throw UnsupportedOperationException("unsupported")
            } ?: defValue
        }
    }

    fun <T : Parcelable> get(key: String, clazz: Class<T>): T? {
        return if (!hasKey(key)) {
            null
        } else {
            instance.decodeParcelable(key, clazz)
        }
    }

    override fun remove(key: String): Boolean {
        instance.remove(key)
        return true
    }

    override fun hasKey(key: String): Boolean {
        return instance.containsKey(key)
    }

    override fun clear() {
        instance.clearAll()
    }
}