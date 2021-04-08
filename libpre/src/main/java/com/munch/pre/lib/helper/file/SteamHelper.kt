@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.pre.lib.helper.file

import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Create by munch1182 on 2021/2/6 15:55.
 */
/**
 * 复制is的内容到os，并在复制完成或者发生异常时尝试关闭流
 *
 * @return 是否完全复制成功
 */
fun InputStream.copyAndClose(os: OutputStream, bufferSize: Int = 8 * 1024): Boolean {
    return try {
        copyTo(os, bufferSize)
        true
    } catch (e: IOException) {
        false
    } finally {
        closeQuietly()
        os.closeQuietly()
    }
}

fun Closeable?.closeQuietly() {
    this ?: return
    try {
        this.close()
    } catch (e: IOException) {
        //
    }
}