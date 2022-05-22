@file:Suppress("unused", "MemberVisibilityCanBePrivate", "NOTHING_TO_INLINE")

package com.munch.lib.log

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * log方法
 * <p>
 * 主要目标是快速使用，复制即用，无需设置tag，参数不限类型不限个数不限
 * <p>
 * 此页不可依赖除基础包之外的其余包，以便复制
 * <p>
 * Create by munch1182 on 2022/3/12 16:46.
 */

const val LOG_DEFAULT = "loglog"

fun log(vararg any: Any?) {
    when {
        any.isEmpty() -> LogLog.offsetMethod(1).log(null)
        any.size == 1 -> LogLog.offsetMethod(1).log(any[0])
        else -> LogLog.offsetMethod(1).log(any)
    }
}

fun Logger.setOnLog(onLog: ((tag: String) -> Unit)? = null) {
    if (onLog == null) {
        setOnLogListener(null)
    } else {
        setOnLogListener(object : Logger.OnLogListener {
            override fun onLog(tag: String) {
                onLog.invoke(tag)
            }
        })
    }
}

fun Logger.setOnPrint(onPrint: ((tag: String, msg: String) -> Unit)? = null) {
    if (onPrint == null) {
        setOnPrintListener(null)
    } else {
        setOnPrintListener(object : Logger.OnPrintListener {
            override fun onPrint(tag: String, log: String) {
                onPrint.invoke(tag, log)
            }
        })
    }
}

/**
 * 全局类
 */
object LogLog : Logger()

sealed class LogStyle {
    object NONE : LogStyle()
    object THREAD : LogStyle()
    object NORMAL : LogStyle()
    object FULL : LogStyle()
}

open class Logger(
    tag: String = LOG_DEFAULT,
    private var enable: Boolean = true,
    private var infoStyle: LogStyle = LogStyle.NORMAL,
) {

    private var tag: String = LOG_DEFAULT
        set(value) {
            if (!value.contains(LOG_DEFAULT)) {
                field = "$value-$LOG_DEFAULT"
            }
        }

    init {
        this.tag = tag
    }

    private var methodOffset = 0
    private var onLog: OnLogListener? = null
    private var onPrint: OnPrintListener? = null

    fun style(style: LogStyle): Logger {
        this.infoStyle = style
        return this
    }

    fun setOnLogListener(onLog: OnLogListener?): Logger {
        this.onLog = onLog
        return this
    }

    fun setOnPrintListener(onPrint: OnPrintListener?): Logger {
        this.onPrint = onPrint
        return this
    }

    fun offsetMethod(offset: Int): Logger {
        methodOffset = offset
        return this
    }

    fun enable(enable: Boolean = false): Logger {
        this.enable = enable
        return this
    }

    open fun log(ifEnable: () -> Any) {
        if (enable) {
            logStr(FMT.any2Str(ifEnable.invoke()))
        }
    }

    open fun log(vararg any: Any?) {
        if (!enable) {
            return
        }
        when {
            any.isEmpty() -> logStr(Str.EMPTY)
            any.size == 1 -> logStr(FMT.any2Str(any[0]))
            else -> logStr(any.joinToString { FMT.any2Str(it) })
        }
    }

    private fun logStr(msg: String) {
        val split = msg.split(FMT.LINE_SEPARATOR)
        if (split.size == 1) {
            when (infoStyle) {
                LogStyle.NONE -> print(split[0])
                LogStyle.THREAD -> print("${split[0]} (${Thread.currentThread().name})")
                LogStyle.NORMAL -> print("${split[0]} (${Thread.currentThread().name}/${dumpStack(1)[0]})")
                LogStyle.FULL -> {
                    print(split[0])
                    print("--- (${Thread.currentThread().name})")
                    dumpStack(2).forEach { print(it) }
                }
            }
        } else {
            split.forEach { print(it) }
            when (infoStyle) {
                LogStyle.NONE -> {}
                LogStyle.THREAD -> print("--- (${Thread.currentThread().name})")
                LogStyle.NORMAL -> print("--- (${Thread.currentThread().name}/${dumpStack(1)[0]})")
                LogStyle.FULL -> {
                    print("--- (${Thread.currentThread().name})")
                    dumpStack(2).forEach { print(it) }
                }
            }
        }

        onLog?.onLog(tag)
    }

    private fun print(msg: String) {
        Log.d(tag, msg)
        onPrint?.onPrint(tag, msg)
    }

    private fun dumpStack(level: Int): Array<String> {
        if (infoStyle == LogStyle.NONE) {
            return arrayOf(Str.EMPTY)
        }
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
            return arrayOf(Str.EMPTY)
        }
        //如果找到后，进行偏移，以输出调用此类的外部方法
        index += (methodOffset + 3)
        if (index < 0 || trace.size <= index) {
            return arrayOf(Str.EMPTY)
        }
        return if (level > 1) {
            trace.map { FMT.fmtStackTrace(it) }.toTypedArray()
        } else {
            arrayOf(FMT.fmtStackTrace(trace[index]))
        }
    }

    protected class Str {
        companion object {
            internal const val EMPTY = ""
        }
    }

    interface OnLogListener {

        fun onLog(tag: String)
    }

    interface OnPrintListener {

        fun onPrint(tag: String, log: String)
    }
}

object FMT {
    val LINE_SEPARATOR = System.getProperty("line.separator") ?: ""
    const val MAX_COUNT_IN_LINE = 550

    fun any2Str(any: Any?): String {
        return when (any) {
            null -> "null"
            is Byte -> String.format("0x%02X", any)
            is Double -> "${any}D"
            is Float -> "${any}F"
            is Char -> "\'$any\'"
            is Number -> any.toString()
            is String -> fmtStr(any)
            is Throwable -> fmtThrowable(any)
            is Iterator<*> -> iterator2Str(any)
            is Iterable<*> -> iterable2Str(any)
            is Array<*> -> any2Str(any.asList())
            is IntArray -> any2Str(any.asIterable())
            is CharArray -> any2Str(any.asIterable())
            is ByteArray -> byteArray2Str(any)
            is BooleanArray -> any2Str(any.asIterable())
            is FloatArray -> any2Str(any.asIterable())
            is DoubleArray -> any2Str(any.asIterable())
            is LongArray -> any2Str(any.asIterable())
            is ShortArray -> any2Str(any.asIterable())
            else -> any.toString()
        }
    }

    fun fmtThrowable(any: Throwable): String {
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
                sb.append("\t").append(fmtStackTrace(it))
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
                sb.append("\t").append(fmtStackTrace(it))
            }
        }
        return sb.toString()
    }

    fun fmtStackTrace(e: StackTraceElement) =
        "${e.className.split(".").last()}#${e.methodName}(${e.fileName}:${e.lineNumber})"

    fun fmtStr(any: String): String {
        return if (canFmtJson(any)) fmtJson(any) else fmtMultiStr(any)
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

    fun fmtMultiStr(any: String): String {
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

    fun fmtJson(any: String): String {
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

    private fun byteArray2Str(any: ByteArray): String {
        val sb = StringBuilder()
        sb.append("[")
        any.forEachIndexed { index, byte ->
            if (index > 0) {
                sb.append(", ")
            }
            sb.append(any2Str(byte))
        }
        sb.append("]")
        return sb.toString()
    }
}

interface ILogger {

    fun setLog(log: Logger? = null)
}