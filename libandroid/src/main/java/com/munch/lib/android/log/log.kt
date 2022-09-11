package com.munch.lib.android.log

import org.json.JSONArray
import org.json.JSONObject

/**
 * 日志类
 * 1. 使用简单
 * 2. 复制简单
 */

/**
 * 作为通用的log方法
 *
 * 如果要包装自己的log方法, 不能直接调用此方法, 应该调用自定义Logger, 否则无法正确找到调用栈中的方法
 */
fun log(vararg any: Any?) {
    L.log(*any)
}

// 全局的日志输出
private object L : Logger()

open class Logger(
    logTag: String? = null,
    private var info: LogInfo = LogInfo.Full
) {

    private var tag: String = logTag?.let { "$it-loglog" } ?: "loglog"
    private var type: Log = Log.Debug
    private var l: OnLogListener? = null

    fun setType(type: Log): Logger {
        this.type = type
        return this
    }

    fun setTag(tag: String): Logger {
        this.tag = tag
        return this
    }

    fun setLogTag(tag: String): Logger {
        this.tag = "$tag-loglog"
        return this
    }

    fun setInfo(info: LogInfo): Logger {
        this.info = info
        return this
    }

    fun setLogListener(l: OnLogListener?): Logger {
        this.l = l
        return this
    }

    /**
     *  现有问题:
     *  1. inline方法不能定位到准确的位置 -> inline的机制导致的
     *  2. 当[any]为多个JsonString是, JsonString会自动换行但是JsonString之间没有换行 -> 多个Json分开传
     *  3. 协程中调用方法时, 方法不能定位到准确的位置 -> 协程的唤醒机制
     */
    fun log(vararg any: Any?) {
        val log = fmtSepOrMultiStr(
            when {
                any.isEmpty() -> ""
                any.size == 1 -> LoggerFMT.any2Str(any[0])
                any.size > 1 -> LoggerFMT.any2Str(any).removeSurrounding("[", "]")
                else -> LoggerFMT.any2Str(any)
            }
        )
        val info = collectInfo()

        if (log.size == 1) {
            print("${log[0]}${info?.let { "  ---(${it})" } ?: ""}")
        } else {
            log.forEach { print(it) }
            info?.let { print("---(${it})") }
        }
    }

    private fun print(msg: String) {
        when (type) {
            Log.Debug -> android.util.Log.d(tag, msg)
            Log.Error -> android.util.Log.e(tag, msg)
            Log.Info -> android.util.Log.i(tag, msg)
        }
        l?.onLog(msg)
    }

    private fun collectInfo(): String? {
        return when (info) {
            LogInfo.Caller -> stackInfo
            LogInfo.Full -> "${threadName}/${stackInfo}"
            LogInfo.None -> null
            LogInfo.Thread -> threadName
        }
    }

    private val threadName: String
        get() = Thread.currentThread().name

    /**
     * VMStack#getThreadStackTrace(VMStack.java:-2)
     * Thread#getStackTrace(Thread.java:1724)
     * Logger#getStackInfo(log.kt:97)
     * Logger#collectInfo(log.kt:78)
     * Logger#log(log.kt:53)
     * LogKt#log(log.kt:7)
     * LogActivity#onCreate(LogActivity.kt:28)
     */
    private val stackInfo: String
        get() = LoggerFMT.any2Str(Thread.currentThread().stackTrace[6])

    /**
     * 1. 将过多的文字分成多段文字
     * 2. 将换行符分成多段文字
     */
    private fun fmtSepOrMultiStr(any: String): Array<String> {
        val array = mutableListOf<String>()
        any.split(LoggerFMT.LINE_SEPARATOR).forEach {
            var index = 0
            while (index < it.length) {
                index += if (it.length - index <= LoggerFMT.MAX_COUNT_IN_LINE) {
                    array.add(it.subSequence(index, it.length).toString())
                    it.length
                } else {
                    array.add(it.subSequence(index, LoggerFMT.MAX_COUNT_IN_LINE + index).toString())
                    LoggerFMT.MAX_COUNT_IN_LINE
                }
            }
        }
        return array.toTypedArray()
    }

    /**
     * 当日志输出时, 方法会回调, 回调所在的线程即调用是的线程
     */
    fun interface OnLogListener {
        fun onLog(log: String)
    }
}

/**
 * 将传入的参数转为String
 */
private object LoggerFMT {
    val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"
    const val MAX_COUNT_IN_LINE = 550

    fun any2Str(any: Any?): String {
        return when (any) {
            //null
            null -> "null"
            //str
            is String -> fmtStr(any)
            //base  type
            is Byte -> String.format("0x%02X", any)
            is Long -> "${any}L"
            is Float -> "${any}F"
            is Char -> "'$any'"
            //iter
            is Iterator<*> -> iterator2Str(any)
            is Iterable<*> -> iterator2Str(any.iterator())
            //ele
            is Throwable -> fmtThrowable(any)
            is StackTraceElement -> fmtStackTrace(any)
            //array
            is Array<*> -> iterator2Str(any.iterator())
            is IntArray -> iterator2Str(any.iterator())
            is BooleanArray -> iterator2Str(any.iterator())
            is CharArray -> iterator2Str(any.iterator())
            is ByteArray -> iterator2Str(any.iterator())
            is LongArray -> iterator2Str(any.iterator())
            is ShortArray -> iterator2Str(any.iterator())
            is FloatArray -> iterator2Str(any.iterator())
            is DoubleArray -> iterator2Str(any.iterator())
            //bae type / class
            else -> any.toString()
        }
    }

    private fun fmtStr(any: String): String {
        if (any.length < MAX_COUNT_IN_LINE) return any // 日常情形不需要判断

        val jsonObj = catch { JSONObject(any).toString(4) }
        if (jsonObj != null) return jsonObj
        val jsonArray = catch { JSONArray(any).toString(4) }
        if (jsonArray != null) return jsonArray

        return any
    }

    private fun fmtThrowable(any: Throwable): String {
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
                if (index > 0) sb.append(LINE_SEPARATOR)
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
                if (index > 0) sb.append(LINE_SEPARATOR)
                index++
                sb.append("\t").append(fmtStackTrace(it))
            }
        }
        return sb.toString()
    }

    private fun fmtStackTrace(e: StackTraceElement) =
        "${e.className.split(".").last()}#${e.methodName}(${e.fileName}:${e.lineNumber})"


    private fun iterator2Str(iterator: Iterator<*>): String {
        val sb = StringBuilder()
        sb.append("[")
        var index = 0
        while (iterator.hasNext()) {
            if (index > 0) sb.append(", ")
            sb.append(any2Str(iterator.next()))
            index++
        }
        sb.append("]")
        return sb.toString()
    }
}

/**
 * 显示类型
 */
sealed class Log {
    // 开发日志
    object Debug : Log()

    // 错误日志
    object Error : Log()

    // 一般显示会被过滤掉的日志
    object Info : Log()
}

/**
 * 显示附带信息
 */
sealed class LogInfo {
    // 只显示log内容
    object None : LogInfo()

    // log内容后显示调用线程
    object Thread : LogInfo()

    // log内容后显示调用位置
    object Caller : LogInfo()

    // log内容后显示调用线程和调用位置
    object Full : LogInfo()
}

private inline fun <T> catch(block: () -> T): T? {
    return try {
        block.invoke()
    } catch (e: Exception) {
        null
    }
}