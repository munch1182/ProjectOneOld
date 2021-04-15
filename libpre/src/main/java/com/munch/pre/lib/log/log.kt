@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.pre.lib.log

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * log方法
 * <p>
 * 主要目标是快速使用，复制即用，无需设置tag，参数不限类型不限个数
 * <p>
 * 此页不可依赖除基础包之外的其余包，以便复制
 * <p>
 * Create by munch1182 on 2021/3/30 16:29.
 */
/**
 * 全局单例类
 */
object LogLog : Logger() {

    override fun logOne(any: Any?) {
        methodOffset++
        super.logOne(any)
        reset()
    }
}

open class Logger {

    open fun log(vararg any: Any?) {
        if (any.size == 1) {
            logOne(any[0])
        } else {
            logOne(any)
        }
    }

    /**
     * 声明即将输出的字符串为json格式
     */
    fun isJson(isJson: Boolean = true): Logger {
        this.isJson = isJson
        return this
    }

    /**
     * 设置调用堆栈的偏移值
     */
    fun methodOffset(offset: Int): Logger {
        this.methodOffset = offset
        return this
    }

    /**
     * @param type [Log.VERBOSE] [Log.DEBUG] [Log.INFO] [Log.WARN] [Log.ERROR]
     */
    fun setLogType(type: Int): Logger {
        this.type = type
        return this
    }

    fun setTag(tag: String): Logger {
        this.tag = tag
        return this
    }

    fun setListener(listener: ((msg: String, thread: Thread) -> Unit)? = null): Logger {
        this.logListener = listener
        return this
    }

    companion object {
        private const val TAG_DEF = "loglog"
        private const val MAX_COUNT_IN_LINE = 300
        private val LINE_SEPARATOR = System.getProperty("line.separator")
    }

    var tag: String? = null
    protected var logListener: ((msg: String, thread: Thread) -> Unit)? = null
    var type = Log.DEBUG
    var methodOffset = 0
    var isJson = false
    var noInfo = false

    protected open fun logOne(any: Any?) {
        val msg = any2Str(any)
        val thread: Thread = Thread.currentThread()
        val traceInfo: String? = if (noInfo) null else dumpTraceInfo()
        type = if (any is Throwable) Log.ERROR else type

        val split = msg.split(LINE_SEPARATOR)
        if (split.size == 1) {
            if (noInfo) {
                print(msg)
            } else {
                print("$msg (${thread.name}/$traceInfo)")
            }
        } else {
            split.forEach { print(it) }
            if (!noInfo) {
                print("--- (${thread.name}/$traceInfo)")
            }
        }
        logListener?.invoke(msg, thread)
    }

    protected open fun reset() {
        tag = TAG_DEF
        type = Log.DEBUG
        methodOffset = 0
        isJson = false
    }

    protected open fun print(msg: String) {
        when (type) {
            Log.DEBUG -> Log.d(tag ?: TAG_DEF, msg)
            Log.ERROR -> Log.e(tag ?: TAG_DEF, msg)
            Log.INFO -> Log.i(tag ?: TAG_DEF, msg)
            Log.WARN -> Log.w(tag ?: TAG_DEF, msg)
            Log.VERBOSE -> Log.v(tag ?: TAG_DEF, msg)
        }
    }

    /**
     * 获取调用本类的方法
     *
     * 20210331：除inline函数外，其余调用都可以找到实际调用处
     */
    protected open fun dumpTraceInfo(): String {
        var index = -1
        val trace = Thread.currentThread().stackTrace
        trace.run {
            forEachIndexed { i, e ->
                if (e.className == Logger::class.qualifiedName) {
                    index = i
                    return@run
                }
            }
        }
        if (index == -1) {
            return Str.EMPTY
        }
        index += (methodOffset + 3)
        if (index < 0 || trace.size <= index) {
            return Str.EMPTY
        }
        val e = trace[index]
        return formatStackTrace(e)
    }

    protected open fun formatStackTrace(e: StackTraceElement) =
        "${e.className}#${e.methodName}(${e.fileName}:${e.lineNumber})"

    protected open fun any2Str(any: Any?): String {
        return when (any) {
            null -> Str.NULL
            is Byte -> String.format("0x%02x", any)
            is Double -> "${any}D"
            is Float -> "${any}F"
            is Char -> "\'$any\'"
            is Number -> any.toString()
            is String -> formatStr(any)
            is Throwable -> formatThrowable(any)
            is Iterator<*> -> iterator2Str(any)
            is Iterable<*> -> iterable2Str(any)
            is Array<*> -> any2Str(any.asList())
            is IntArray -> any2Str(any.asIterable())
            is CharArray -> any2Str(any.asIterable())
            is ByteArray -> any2Str(any.asIterable())
            is BooleanArray -> any2Str(any.asIterable())
            is FloatArray -> any2Str(any.asIterable())
            is DoubleArray -> any2Str(any.asIterable())
            is LongArray -> any2Str(any.asIterable())
            is ShortArray -> any2Str(any.asIterable())
            else -> any.toString()
        }
    }

    protected open fun formatThrowable(any: Throwable): String {
        val sb = StringBuilder()
        val cause = any.cause
        sb.append("EXCEPTION: [")
        sb.append(any.javaClass.canonicalName)
            .append(":")
            .append(any.message)
            .append("]")
            .append(LINE_SEPARATOR)
        if (cause == null) {
            any.stackTrace.forEach {
                sb.append("\t").append(formatStackTrace(it)).append(LINE_SEPARATOR)
            }
        } else {
            sb.append("CAUSED: [")
            sb.append(any.javaClass.canonicalName)
                .append(":")
                .append(cause.message)
                .append("]")
                .append(LINE_SEPARATOR)
            cause.stackTrace.forEach {
                sb.append("\t").append(formatStackTrace(it)).append(LINE_SEPARATOR)
            }
        }
        return sb.toString()
    }

    protected open fun formatStr(any: String): String {
        return if (isJson) formatJson(any) else formatMultiStr(any)
    }

    protected open fun formatMultiStr(any: String): String {
        if (any.length < MAX_COUNT_IN_LINE) {
            return "\"$any\""
        } else {
            val sb = StringBuilder()
            any.split(LINE_SEPARATOR).forEach {
                var index = 0
                while (index < it.length) {
                    index += if (it.length - index < MAX_COUNT_IN_LINE) {
                        sb.append(it.subSequence(index, it.length)).append(LINE_SEPARATOR)
                        it.length
                    } else {
                        sb.append(it.subSequence(index, MAX_COUNT_IN_LINE)).append(LINE_SEPARATOR)
                        MAX_COUNT_IN_LINE
                    }
                }
            }
            return "\"$sb\""
        }
    }

    protected open fun formatJson(any: String): String {
        return try {
            when {
                any.startsWith("{") -> JSONObject(any).toString(4)
                any.startsWith("[") -> JSONArray(any).toString(4)
                else -> throw IllegalStateException("cannot format json start with ${any[0]}")
            }
        } catch (e: JSONException) {
            "cannot format json : ${e.message}"
        } catch (e: IllegalStateException) {
            e.message!!
        }
    }

    protected open fun iterable2Str(iterable: Iterable<*>): String {
        return iterator2Str(iterable.iterator())
    }

    protected open fun iterator2Str(iterator: Iterator<*>): String {
        val sb = StringBuilder()
        sb.append("[")
        var index = 0
        var sep = false
        while (iterator.hasNext()) {
            if (index > 0) {
                sb.append(", ")
                if (sep) {
                    sb.append(LINE_SEPARATOR)
                }
            }
            val str = any2Str(iterator.next())
            sep = str.length >= 20
            sb.append(str)
            index++
        }
        sb.append("]")
        return sb.toString()
    }

    protected class Str {
        companion object {
            internal const val NULL = "null"
            internal const val EMPTY = ""
        }
    }
}

/**
 * Create by munch1182 on 2021/3/31 13:40.
 */
fun log(vararg any: Any?) {
    val a = when {
        any.isEmpty() -> ""
        any.size == 1 -> any[0]
        else -> any
    }
    LogLog.methodOffset(1).log(a)
}

fun logJson(json: String) {
    LogLog.methodOffset(1).isJson().log(json)
}