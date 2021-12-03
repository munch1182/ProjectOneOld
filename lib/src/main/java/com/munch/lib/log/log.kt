@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.log

import android.util.Log
import com.munch.lib.base.toHexStr
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

open class Logger(
    var tag: String? = null,
    var noStack: Boolean = false,
    var noInfo: Boolean = false
) {

    open fun withEnable(func: () -> Any?) {
        if (enable) {
            logOne(func.invoke())
        }
    }

    open fun log(vararg any: Any?) {
        if (any.size == 1) {
            logOne(any[0])
        } else {
            logOne(any)
        }
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
    }

    protected var logListener: ((msg: String, thread: Thread) -> Unit)? = null
    var type = Log.DEBUG
    var methodOffset = 0
    var enable = true

    protected open fun logOne(any: Any?) {
        if (!enable) {
            return
        }
        val msg = FMT.any2Str(any)
        val thread: Thread = Thread.currentThread()
        val traceInfo: String? = if (noInfo) null else dumpTraceInfo()

        val split = msg.split(FMT.LINE_SEPARATOR)
        if (split.size == 1) {
            when {
                noInfo -> print(msg)
                noStack -> print("$msg (${thread.name})")
                else -> print("$msg (${thread.name}/$traceInfo)")
            }
        } else {
            split.forEach { print(it) }
            if (!noInfo) {
                print("--- (${thread.name}/$traceInfo)")
            } else if (noStack) {
                print("--- (${thread.name})")
            }
        }
        logListener?.invoke(msg, thread)
    }

    protected open fun reset() {
        tag = null
        type = Log.DEBUG
        methodOffset = 0
    }

    protected open fun print(msg: String) {
        val tag = if (this.tag != null) "${this.tag}-$TAG_DEF" else TAG_DEF
        when (type) {
            Log.DEBUG -> Log.d(tag, msg)
            Log.ERROR -> Log.e(tag, msg)
            Log.INFO -> Log.i(tag, msg)
            Log.WARN -> Log.w(tag, msg)
            Log.VERBOSE -> Log.v(tag, msg)
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
        return FMT.formatStackTrace(e)
    }

    protected class Str {
        companion object {
            internal const val EMPTY = ""
        }
    }
}

object FMT {
    val LINE_SEPARATOR = System.getProperty("line.separator") ?: ""
    const val MAX_COUNT_IN_LINE = 450

    fun any2Str(any: Any?): String {
        return when (any) {
            null -> "null"
            is Byte -> any.toHexStr()
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
            is ByteArray -> any.toHexStr()
            is BooleanArray -> any2Str(any.asIterable())
            is FloatArray -> any2Str(any.asIterable())
            is DoubleArray -> any2Str(any.asIterable())
            is LongArray -> any2Str(any.asIterable())
            is ShortArray -> any2Str(any.asIterable())
            else -> any.toString()
        }
    }

    fun formatThrowable(any: Throwable): String {
        val sb = StringBuilder()
        val cause = any.cause
        sb.append("EXCEPTION: [")
        sb.append(any.javaClass.canonicalName)
            .append(":")
            .append(any.message)
            .append("]")
            .append(LINE_SEPARATOR)
        var index = 0
        if (cause == null) {
            any.stackTrace.forEach {
                if (index > 0) {
                    sb.append(LINE_SEPARATOR)
                }
                index++
                sb.append("\t").append(formatStackTrace(it))
            }
        } else {
            sb.append("CAUSED: [")
            sb.append(any.javaClass.canonicalName)
                .append(":")
                .append(cause.message)
                .append("]")
                .append(LINE_SEPARATOR)
            cause.stackTrace.forEach {
                if (index > 0) {
                    sb.append(LINE_SEPARATOR)
                }
                index++
                sb.append("\t").append(formatStackTrace(it))
            }
        }
        return sb.toString()
    }

    fun formatStackTrace(e: StackTraceElement) =
        "${e.className.split(".").last()}#${e.methodName}(${e.fileName}:${e.lineNumber})"

    fun formatStr(any: String): String {
        return if (canFmtJson(any)) formatJson(any) else formatMultiStr(any)
    }

    fun canFmtJson(any: String): Boolean {
        return try {
            JSONObject(any)
            true
        } catch (_: Exception) {
            try {
                JSONArray(any)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun formatMultiStr(any: String): String {
        if (any.length <= MAX_COUNT_IN_LINE) {
            return "\"$any\""
        } else {
            val sb = StringBuilder()
            any.split(LINE_SEPARATOR).forEach {
                var index = 0
                while (index < it.length) {
                    index += if (it.length - index <= MAX_COUNT_IN_LINE) {
                        sb.append(it.subSequence(index, it.length)).append(LINE_SEPARATOR)
                        it.length
                    } else {
                        sb.append(it.subSequence(index, MAX_COUNT_IN_LINE + index))
                            .append(LINE_SEPARATOR)
                        MAX_COUNT_IN_LINE
                    }
                }
            }
            return "\"${sb.toString().removeSuffix(LINE_SEPARATOR)}\""
        }
    }

    fun formatJson(any: String): String {
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

    fun iterable2Str(iterable: Iterable<*>): String {
        return iterator2Str(iterable.iterator())
    }

    fun iterator2Str(iterator: Iterator<*>): String {
        val sb = StringBuilder()
        sb.append("[")
        var index = 0
        while (iterator.hasNext()) {
            if (index > 0) {
                sb.append(", ")
            }
            val str = any2Str(iterator.next())
            sb.append(str)
            index++
        }
        sb.append("]")
        return sb.toString()
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