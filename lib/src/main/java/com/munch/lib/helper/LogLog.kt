package com.munch.lib.helper

import android.util.Log

/**
 * Create by munch1182 on 2020/12/8 16:04.
 */
object LogLog {

    private const val TAG = "loglog"

    private var tag: String? = null
    private var className: String? = null
    private var listener: LogListener? = null

    fun setListener(listener: LogListener): LogLog {
        this.listener = listener
        return this
    }

    @FunctionalInterface
    interface LogListener {
        fun onLog(tag: String, msg: String)
    }

    fun tag(tag: String = TAG): LogLog {
        this.tag = tag
        return this
    }

    /**
     * 如果对本类进行了包装，需要调用此方法，以准确找到实际调用方法
     * @param clazz 包装类
     */
    fun callClass(clazz: Class<*>): LogLog {
        className = clazz.canonicalName
        return this
    }

    @JvmStatic
    fun log(vararg any: Any?) {
        var log = if (any.size == 1) {
            any2Str(any[0])
        } else {
            val builder = StringBuilder()
            any.forEach {
                builder.append(any2Str(it))
                    .append(Str.STR_SEMICOLON)
            }
            builder.toString()
        }
        log = "${Thread.currentThread().name}: $log ---${getCallFunction()}"
        val tag: String = tag ?: TAG
        Log.d(tag, log)
        listener?.onLog(tag, log)
        className = null
        this.tag = null
    }

    private fun getCallFunction(): String {
        val trace = Thread.currentThread().stackTrace
        var lastIndex = -1
        trace.forEachIndexed { index, element ->
            if (element.className == className ?: LogLog.javaClass.canonicalName) {
                lastIndex = index
                return@forEachIndexed
            }
        }
        if (className == null) {
            lastIndex++
        }
        if (lastIndex == -1) {
            return ""
        }
        val e = trace[lastIndex]
        return "${e.className}#${e.methodName}(${e.fileName}:${e.lineNumber})"
    }

    private fun any2Str(any: Any?): String {
        return when (any) {
            null -> Str.STR_NULL
            is Double -> "${any}D"
            is Float -> "${any}F"
            is Char -> "\'$any\'"
            is Number -> any.toString()
            is String -> "\"$any\""
            is Iterable<*> -> {
                val builderTemp = StringBuilder()
                val lastSplit = "${Str.STR_SPLIT}${Str.STR_BLANK}"
                any.forEach {
                    builderTemp.append(any2Str(it))
                        .append(lastSplit)
                }
                var str = builderTemp.toString()
                str = if (str.endsWith(lastSplit)) {
                    str.substring(0, str.lastIndexOf(lastSplit))
                } else {
                    str
                }
                return "[${Str.STR_BLANK}$str${Str.STR_BLANK}]"
            }
            is Array<*> -> any2Str(any.asIterable())
            is IntArray -> any2Str(any.asIterable())
            is CharArray -> any2Str(any.asIterable())
            is ByteArray -> any2Str(any.asIterable())
            is BooleanArray -> any2Str(any.asIterable())
            is FloatArray -> any2Str(any.asIterable())
            is DoubleArray -> any2Str(any.asIterable())
            is LongArray -> any2Str(any.asIterable())
            is ShortArray -> any2Str(any.asIterable())
            else -> any.toString()
        }.trim()
    }

    internal object Str {
        const val STR_NULL = "null"
        const val STR_SPLIT = ","
        const val STR_BLANK = "  "
        const val STR_SEMICOLON = ";  "
    }

}