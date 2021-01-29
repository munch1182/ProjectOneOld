package com.munch.lib.helper

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.munch.lib.BaseApp
import java.io.File
import java.io.FileWriter


/**
 * 用于流程日志，主要区别在于单独tag且可以写进文件中
 *
 * 如果要写入文件，需要在适当位置调用[start]和[end]
 *
 * 并没有考虑并发
 *
 * Create by munch1182 on 2021/1/25 14:39.
 */
class ProcedureLog(
    private val tag: String,
    private val clazz: Class<*>,
    @LogLog.Type private val type: Int = LogLog.TYPE_DEBUG,
    back2File: Boolean = false,
    private var file: File = File(BaseApp.getContext().cacheDir, "procedure_log_${tag}.txt")
) : LifecycleOwner {

    private fun getLog() = LogLog.tag(tag).type(type).callClass(clazz)
    private val lifecycle = LifecycleRegistry(this)

    init {
        //初始化生命周期
        lifecycle.currentState = Lifecycle.State.DESTROYED
        if (back2File) {
            setCanWrite2File()
        }
    }

    private fun setCanWrite2File() {
        file = file.checkOrNew() ?: return
        getLog().setListener(this) { tag, msg ->
            if (this.tag != tag) {
                return@setListener
            }
            FileWriter(file, true).use {
                val formatDate =
                    "yyyy-MM-dd HH:mm:ss".formatDate(System.currentTimeMillis())
                it.write("$formatDate: ")
                it.write("\n")
                it.write(msg)
                it.write("\n")
            }
        }
    }

    fun start() {
        lifecycle.currentState = Lifecycle.State.CREATED
        getLog().log("============↓procedure $tag start↓===========")
    }

    fun end() {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }
        getLog().log("============↑procedure $tag end↑===========")
        lifecycle.currentState = Lifecycle.State.DESTROYED
    }

    fun step(vararg any: Any) {
        when {
            any.isEmpty() -> {
                getLog().log(null)
            }
            any.size == 1 -> {
                getLog().log(any[0])
            }
            else -> {
                getLog().log(any)
            }
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycle
    }
}