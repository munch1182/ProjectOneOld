package com.munch.lib.log

import android.util.Log
import androidx.annotation.IntRange

/**
 * 要求：即粘即用
 *      细化配置
 *      准确显示栈信息
 *      长文本显示
 * Created by Munch on 2019/7/3 14:38
 */
object LogLog {

    @JvmStatic
    fun log(vararg any: Any?) = printFinMessage(getFinBuilder(null), any)

    @JvmStatic
    fun log(builder: Builder? = null, vararg any: Any?) = printFinMessage(getFinBuilder(builder), any)

    private const val MAX_LENGTH = 3000

    private fun printFinMessage(builder: Builder, any: Array<out Any?>) {
        val tag = builder.getTag()
        val type = builder.logType
        val border = builder.logBorder
        //上边框
        if (border) {
            print(tag, type, Border.BORDER_TOP)
        }
        //显示调用方法
        if (builder.logStack) {
            val list = getStackMessage(builder.stackCount, builder.stackClass)
            if (list.isNotEmpty()) {
                list.forEach {
                    print(tag, type, if (border) Border.BORDER_START.plus(it) else it)
                }
            }
        }
        //内容
        if (any.isNotEmpty()) {
            val str = if (any.size == 1) {
                //一个参数时
                any2Str(any[0])
            } else {
                //多个参数
                any2Str(any)
            }
            if (str.length <= MAX_LENGTH) {
                print(tag, type, if (border) Border.BORDER_START.plus(str) else str)
            } else {
                splitByLength(str).forEach {
                    print(tag, type, if (border) Border.BORDER_START.plus(it) else it)
                }
            }

        }
        //下边框
        if (border) {
            print(tag, type, Border.BORDER_BOTTOM)
        }
    }

    private fun splitByLength(str: String): List<String> {
        val count = str.length / MAX_LENGTH
        if (count == 0) {
            return arrayListOf(str)
        }
        val list = ArrayList<String>(count + 1)
        for (i in 0 until count) {
            list.add(str.substring(i * MAX_LENGTH, (i + 1) * MAX_LENGTH))
        }
        list.add(str.substring(count * MAX_LENGTH))
        return list
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
        }
    }

    /**
     * 获取调用的栈信息
     */
    private fun getStackMessage(stackCount: Int, stackClass: Class<*>): List<String> {
        val trace = Thread.currentThread().stackTrace
        if (trace.isEmpty()) {
            return emptyList()
        }
        val callStackTraceElement = getCallStackTraceElement(trace, stackCount, stackClass)
        if (callStackTraceElement.isEmpty()) {
            return emptyList()
        }
        val list = ArrayList<String>(callStackTraceElement.size)
        callStackTraceElement.forEachIndexed { index, stackTraceElement ->
            /*clearBuild()*/
            val builder = java.lang.StringBuilder()
            for (i in 0 until index) {
                builder.append(Str.STR_BLANK)
            }
            list.add(
                builder.append(stackTraceElement.className)
                    .append("#")
                    .append(stackTraceElement.methodName)
                    .append("(")
                    .append(stackTraceElement.fileName)
                    .append(":")
                    .append(stackTraceElement.lineNumber)
                    .append(")")
                    .append(" in thread: ")
                    .append(Thread.currentThread().name)
                    .toString()
            )
        }
        return list
    }

    private fun getCallStackTraceElement(
        trace: Array<StackTraceElement>,
        stackCount: Int,
        stackClass: Class<*>
    ): List<StackTraceElement> {
        var lastIndex = -1
        trace.forEachIndexed { index, element ->
            if (element.className == stackClass.canonicalName) {
                lastIndex = index
            } else if (lastIndex != -1) {
                return@forEachIndexed
            }
        }
        if (lastIndex == -1) {
            return emptyList()
        }
        lastIndex++
        if (stackCount == 1) {
            return arrayListOf(trace[lastIndex])
        }
        val list = ArrayList<StackTraceElement>(stackCount)
        for (i in 0 until stackCount) {
            if (trace.size <= lastIndex + i) {
                return list
            }
            list.add(trace[lastIndex + i])
        }
        return list
    }

    private fun print(tag: String, type: Int, str: String) {
        when (type) {
            0 -> Log.v(tag, str)
            1 -> Log.d(tag, str)
            2 -> Log.i(tag, str)
            3 -> Log.w(tag, str)
            4 -> Log.e(tag, str)
            else -> Log.d(tag, str)
        }
    }

    private fun getFinBuilder(builder: Builder?): Builder = builder ?: BuilderSingleton.INSTANCE

    class Builder {
        companion object {
            const val DEF_TAG = "LogLog"
        }

        private val tagThreadLocal by lazy(mode = LazyThreadSafetyMode.NONE) { ThreadLocal<String>() }
        var logStack = true
        var logBorder = true
        var logType = 1
        var stackCount = 1
        var stackClass: Class<*> = LogLog.javaClass

        /**
         * 调用的LogLog的类，如果直接调用了此类，则无需调用，如果使用了包装类，则需要传入包装类
         */
        fun stackClass(clazz: Class<*>): Builder {
            stackClass = clazz
            return this
        }

        /**
         * 显示的栈数量
         */
        fun stackCount(@IntRange(from = 0) count: Int): Builder {
            stackCount = count
            return this
        }

        /**
         * v,d,i,w,e -> 0,1,2,3,4
         */
        fun logType(@IntRange(from = 0, to = 4) type: Int): Builder {
            logType = type
            return this
        }

        fun tag(tag: String): Builder {
            tagThreadLocal.set(tag)
            return this
        }

        /**
         * 是否显示调用的方法
         */
        fun logStack(log: Boolean = true): Builder {
            logStack = log
            return this
        }

        /**
         * 是否显示边框
         */
        fun logBorder(log: Boolean = true): Builder {
            logBorder = log
            return this
        }

        fun getTag(): String {
            return tagThreadLocal.get() ?: DEF_TAG
        }
    }

    private object BuilderSingleton {
        val INSTANCE = Builder()
    }

    internal object Border {
        const val BORDER_TOP =
            "╔═══════════════════════════════════════════════════════════════════════════════════════════════════"
        const val BORDER_START = "║ "
        const val BORDER_BOTTOM =
            "╚═══════════════════════════════════════════════════════════════════════════════════════════════════"
    }

    internal object Str {
        const val STR_NULL = "null"
        const val STR_SPLIT = ","
        const val STR_BLANK = "  "
    }
}