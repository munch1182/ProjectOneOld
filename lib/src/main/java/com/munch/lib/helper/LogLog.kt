package com.munch.lib.helper

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * 此页不可依赖除基础包之外的其余包
 * 以便复制
 * Create by munch1182 on 2020/12/8 16:04.
 */
object LogLog {

    private const val TAG = "loglog"
    private const val LINE_MAX_CHAR = 300

    private var notPrint: Boolean = false
    private var tag: String? = null
    private var className: String? = null
    private val listeners: ArrayList<(tag: String, msg: String) -> Unit> = arrayListOf()
    private var maxChar: Int? = null

    fun addListener(func: (tag: String, msg: String) -> Unit): LogLog {
        listeners.add(func)
        return this
    }

    fun setListener(owner: LifecycleOwner, func: (tag: String, msg: String) -> Unit): LogLog {
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                addListener(func)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                removeListener(func)
                owner.lifecycle.removeObserver(this)
            }
        })
        return this
    }

    fun removeListener(func: (tag: String, msg: String) -> Unit): LogLog {
        listeners.remove(func)
        return this
    }

    /**
     * 全局方法，关闭或者开启log
     */
    fun notPrint(notPrint: Boolean = true) {
        this.notPrint = notPrint
    }

    /**
     * 调用生效
     */
    fun tag(tag: String = TAG): LogLog {
        this.tag = tag
        return this
    }

    /**
     * 全局设置
     */
    fun maxCharInLine(max: Int): LogLog {
        this.maxChar = max
        return this
    }

    private fun getMaxCharInLine() = maxChar ?: LINE_MAX_CHAR

    /**
     * 如果对本类进行了包装，需要调用此方法，以准确找到实际调用方法
     *
     * 调用生效
     * @param clazz 包装类
     */
    fun callClass(clazz: Class<*>): LogLog {
        className = clazz.canonicalName
        return this
    }

    @JvmStatic
    fun log(vararg any: Any?) {
        if (notPrint) {
            return
        }
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
        val tag: String = tag ?: TAG
        if (log.length < getMaxCharInLine()) {
            Log.d(tag, "${Thread.currentThread().name}: $log ---${getCallFunction()}")
        } else {
            log = "${Thread.currentThread().name}: $log"
            more2line(log).forEach {
                Log.d(tag, it)
            }
            Log.d(tag, "---${getCallFunction()}")
        }
        this.listeners.forEach {
            it.invoke(tag, log)
        }
        this.className = null
        this.tag = null
    }

    private fun more2line(str: String): ArrayList<String> {
        val maxCharInLine = getMaxCharInLine()
        if (str.length < maxCharInLine) {
            return arrayListOf(str)
        }
        var line = str.length / maxCharInLine
        if (line * maxCharInLine < str.length) {
            line += 1
        }
        val list = ArrayList<String>(line)
        var endIndex = 0
        for (i in 0 until line - 1) {
            endIndex = maxCharInLine * i
            val element = str.substring(endIndex, maxCharInLine + endIndex)
            list.add(element)
        }
        list.add(str.substring(maxCharInLine + endIndex, str.length))
        return list
    }

    private fun getCallFunction(): String {
        val trace = Thread.currentThread().stackTrace
        var lastIndex = -1
        val name: String = className ?: LogLog.javaClass.canonicalName ?: "LogLog"
        kotlin.run outside@{
            var className: String
            //正序遇到的第一个包内的类即最后调用的类的方法
            trace.forEachIndexed { index, element ->
                className = element.className
                //替换内部类符号
                className = className.replace("$", ".")
                if (className.contains(name)) {
                    lastIndex = index
                    return@outside
                }
            }
        }
        //但trace包含了[getCallFunction]和[log]两个方法，所以要+2去找调用了[log]的方法
        if (className == null) {
            lastIndex += 2
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