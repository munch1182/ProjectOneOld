package com.munch.lib.helper

import androidx.annotation.WorkerThread
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Create by munch1182 on 2022/2/22 15:44.
 */
object ExecHelper {

    @WorkerThread
    @Throws(IOException::class)
    fun exec(cmd: String): String {
        val runtime = Runtime.getRuntime()
        val process = runtime.exec(cmd)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val sb = StringBuilder()
        val buffer = CharArray(1024)
        var ch: Int
        ch = reader.read(buffer)
        while (ch != -1) {
            sb.append(buffer, 0, ch)
            ch = reader.read(buffer)
        }
        return sb.toString()
    }
}