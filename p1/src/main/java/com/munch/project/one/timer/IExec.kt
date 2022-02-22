package com.munch.project.one.timer

import com.munch.lib.app.AppHelper
import com.munch.project.one.test.Log2FileByIOHelper
import java.io.File

/**
 * Create by munch1182 on 2022/2/22 16:59.
 */
interface IExec {

    fun exec(timer: Timer)
}

class ExecImp(private val name: String) : IExec {

    private val recordImp by lazy {
        Log2FileByIOHelper(AppHelper.app.cacheDir) { dir -> File(dir, name) }
    }

    override fun exec(timer: Timer) {
        recordImp.write("$name 执行了 $timer\r")
    }
}