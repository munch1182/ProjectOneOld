package com.munch.pre.lib.log

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * log方法
 * <p>
 * 主要目标是快速使用，复制即用，无需设置tag，参数不限类型不限个数
 * <p>
 * 此页不可依赖除基础包之外的其余包，以便复制
 * <p>
 * Create by munch1182 on 2021/3/30 16:29.
 */
object LogLog {

    fun log(vararg any: Any?) {
        if (any.size == 1) {
            logOne(any[0])
        } else {
            logOne(any)
        }
    }

    /**
     * 声明即将输出的字符串为json格式
     */
    fun isJson(isJson: Boolean = true): LogLog {
        this.isJson = isJson
        return this
    }

    /**
     * 设置调用堆栈的偏移值
     */
    fun methodOffset(offset: Int): LogLog {
        this.methodOffset = offset
        return this
    }

    /**
     * @param type [Log.VERBOSE] [Log.DEBUG] [Log.INFO] [Log.WARN] [Log.ERROR]
     */
    fun setLogType(type: Int): LogLog {
        this.type = type
        return this
    }

    fun setTag(tag: String): LogLog {
        this.tag = tag
        return this
    }

    fun setListener(listener: ((msg: String, thread: Thread) -> Unit)? = null): LogLog {
        this.logListener = listener
        return this
    }

    private const val TAG_DEF = "loglog"
    private var tag: String? = null
    private val sb = StringBuilder()
    private var logListener: ((msg: String, thread: Thread) -> Unit)? = null
    private var type = Log.DEBUG
    private var methodOffset = 0
    private var isJson = false
    private val LINE_SEPARATOR = System.getProperty("line.separator")
    private const val MAX_COUNT_IN_LINE = 300

    private fun logOne(any: Any?) {
        val msg = any2Str(any)
        val thread = Thread.currentThread()
        val traceInfo = dumpTraceInfo()
        type = if (any is Throwable) Log.ERROR else type

        val split = msg.split(LINE_SEPARATOR)
        if (split.size == 1) {
            print("$msg (${thread.name}/$traceInfo)")
        } else {
            split.forEach { print(it) }
            print("--- (${thread.name}/$traceInfo)")
        }
        logListener?.invoke(msg, thread)
        reset()
    }

    private fun reset() {
        tag = TAG_DEF
        type = Log.DEBUG
        methodOffset = 0
        isJson = false
    }

    private fun print(msg: String) {
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
    private fun dumpTraceInfo(): String {
        var index = -1
        val trace = Thread.currentThread().stackTrace
        trace.run {
            forEachIndexed { i, e ->
                if (e.className == LogLog::class.qualifiedName) {
                    index = i
                    return@run
                }
            }
        }
        if (index == -1) {
            return Str.EMPTY
        }
        index += 3 + methodOffset
        if (index < 0 || trace.size <= index) {
            return Str.EMPTY
        }
        val e = trace[index]
        return formatStackTrace(e)
    }

    private fun formatStackTrace(e: StackTraceElement) =
        "${e.className}#${e.methodName}(${e.fileName}:${e.lineNumber})"

    private fun any2Str(any: Any?): String {
        return when (any) {
            null -> Str.NULL
            is Byte -> String.format("0x%02x", any)
            is Double -> "${any}D"
            is Float -> "${any}F"
            is Char -> "\'$any\'"
            is Number -> any.toString()
            is String -> formatStr(any)
            is Throwable -> formatThrowable(any)
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

    private fun formatThrowable(any: Throwable): String {
        sb.clear()
        val cause = any.cause
        sb.append("EXCEPTION: [")
        sb.append(any.message).append("]").append(LINE_SEPARATOR)
        if (cause == null) {
            any.stackTrace.forEach { sb.append(formatStackTrace(it)).append(LINE_SEPARATOR) }
        } else {
            sb.append("CAUSED: [")
            sb.append(cause.message).append("]").append(LINE_SEPARATOR)
            cause.stackTrace.forEach {
                sb.append(formatStackTrace(it)).append(LINE_SEPARATOR)
            }
        }
        return sb.toString()
    }

    private fun formatStr(any: String): String {
        return if (isJson) formatJson(any) else formatMultiStr(any)
    }

    private fun formatMultiStr(any: String): String {
        sb.clear()
        any.split(LINE_SEPARATOR).forEach {
            var index = 0
            while (index < it.length) {
                if (it.length < MAX_COUNT_IN_LINE) {
                    sb.append(it.subSequence(index, it.length)).append(LINE_SEPARATOR)
                } else {
                    sb.append(it.subSequence(index, MAX_COUNT_IN_LINE))
                    index += MAX_COUNT_IN_LINE
                }
            }
        }
        return "\"$sb\""
    }

    private fun formatJson(any: String): String {
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

    private fun iterable2Str(iterable: Iterable<*>): String {
        sb.clear()
        return iterable.joinTo(sb, ", ", "[", "]", transform = { any2Str(it) }).toString()
    }

    private class Str {
        companion object {
            internal const val NULL = "null"
            internal const val EMPTY = ""
        }
    }
}