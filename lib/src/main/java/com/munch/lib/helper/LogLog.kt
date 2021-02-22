@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper

import android.util.Log
import androidx.annotation.IntDef
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
    private var simple: Boolean = false
    private var type = 3
    const val TYPE_ERROR = 0
    const val TYPE_WARN = 1
    const val TYPE_INFO = 2
    const val TYPE_DEBUG = 3
    const val TYPE_VERBOSE = 4

    @IntDef(
        TYPE_VERBOSE,
        TYPE_DEBUG,
        TYPE_WARN,
        TYPE_ERROR,
        TYPE_INFO
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type

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

    /**
     * 调用生效
     * 用于简化log，不再输出调用方法
     */
    fun simple(simple: Boolean = true): LogLog {
        this.simple = simple
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
        className = clazz.name
        return this
    }

    fun type(@Type type: Int = TYPE_DEBUG): LogLog {
        this.type = type
        return this
    }

    @JvmStatic
    fun log(vararg any: Any?) {
        if (notPrint) {
            return
        }
        val log = if (any.size == 1) {
            any2Str(any[0])
        } else {
            val builder = StringBuilder()
            any.forEach {
                builder.append(any2Str(it))
                    .append(Str.STR_SEMICOLON)
            }
            builder.toString()
        }
        val name = Thread.currentThread().name
        var msg: String
        if (log.length < getMaxCharInLine()) {
            msg = if (simple) "$name: $log" else "$name: $log ---${getCallFunction()}"
            log(msg)
        } else {
            more2line(log).forEach {
                msg = "$name:$it"
                log(msg)
            }
            if (!simple) {
                msg = "$name: ---${getCallFunction()}"
                log(msg)
            }
        }
        this.className = null
        this.tag = null
        this.simple = false
        this.type = TYPE_DEBUG
    }

    private fun notify(tag: String, msg: String) {
        this.listeners.forEach {
            it.invoke(tag, msg)
        }
    }

    fun getTag() = tag ?: TAG

    private fun log(msg: String) {
        val tag: String = getTag()
        when (type) {
            TYPE_VERBOSE -> Log.v(tag, msg)
            TYPE_DEBUG -> Log.d(tag, msg)
            TYPE_INFO -> Log.i(tag, msg)
            TYPE_WARN -> Log.w(tag, msg)
            TYPE_ERROR -> Log.e(tag, msg)
        }
        notify(tag, msg)
    }

    private fun more2line(str: String): List<String> {
        val maxCharInLine = getMaxCharInLine()
        if (str.length < maxCharInLine) {
            return arrayListOf(str)
        }
        if (str.contains(Str.STR_SPLIT)) {
            val split = str.split(Str.STR_SPLIT)
            return split.mapIndexed { index, s ->
                if (index != split.size - 1) "$s${Str.STR_SPLIT}" else s
            }
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
        val name: String =
            className?.replace("$", ".") ?: LogLog.javaClass.name ?: "LogLog"
        kotlin.run outside@{
            var className: String
            //正序遇到的第一个包内的类即最后调用的类的方法
            trace.forEachIndexed { index, element ->
                //替换内部类符号
                className = element.className.replace("$", ".")
                if (className.contains(name)) {
                    lastIndex = index
                    return@outside
                }
            }
        }
        //直接调用时去掉getStackTrace的两个方法
        if (className == null) {
            lastIndex += 2
        }
        if (lastIndex == -1) {
            return ""
        }
        if (lastIndex !in trace.indices) {
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