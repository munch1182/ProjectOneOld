@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.helper

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import com.munch.lib.Key
import com.munch.lib.task.Data
import com.munch.lib.task.ITask
import com.munch.lib.task.Result
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Create by munch1182 on 2022/5/14 20:05.
 */
object FileHelper {

    const val BYTE = 1024L

    const val KB = 1024 * BYTE

    const val MB = 1024 * KB

    const val GB = 1024 * MB

    /**
     * 将文件转为uri
     *
     * Android7.0以上需要设置FileProvider
     *
     * @see [https://developer.android.google.cn/training/secure-file-sharing/setup-sharing]
     *
     */
    fun toUri(context: Context, file: File): Uri? {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
    }

    /**
     * 将uri的数据复制到dir目录下的同名文件中
     *
     * 如果未查到文件名也将为空
     */
    fun uri2File(
        context: Context,
        uri: Uri?,
        dir: File = context.cacheDir,
        name: String? = null,
        onProgress: OnProgressListener? = null
    ): File? {
        uri ?: return null
        val n = name ?: queryName(context, uri) ?: return null
        val file = File(dir, n)
        context.contentResolver.openInputStream(uri)?.use {
            if (!copy2File(it, file, false, onProgress = onProgress)) {
                return null
            }
        }
        return file
    }

    /**
     * 获取uri的文件名
     */
    fun queryName(context: Context, uri: Uri?): String? {
        uri ?: return null
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)
            ?.apply {
                try {
                    moveToFirst()
                    val index = getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index > -1) {
                        name = getString(index)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            ?.close()
        return name
    }

    /**
     * 将ins中的数据复制到file中
     *
     * @param ins 将要写入的数据
     * @param file 将要被写入的文件
     * @param append 是否是追加写入
     * @param size 缓存数量
     * @param onProgress 进度回调
     */
    @WorkerThread
    fun copy2File(
        ins: InputStream,
        file: File,
        append: Boolean = false,
        size: Int = buffSize(ins.available().toLong()),
        onProgress: OnProgressListener? = null
    ): Boolean {
        return try {
            FileOutputStream(file, append).use { os ->
                val buffer = ByteArray(size)
                var length: Int
                val all = ins.available().toLong()
                var progress = 0L
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                    progress += length
                    onProgress?.onProgress(progress, all)
                }
                os.flush()
                if (progress < all) {
                    onProgress?.onProgress(all, all)
                }
                true
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    fun buffSize(size: Long): Int {
        val mb4 = 4 * MB
        return when {
            size > GB -> mb4.toInt()
            size > mb4 -> MB.toInt()
            else -> 4096
        }
    }

    interface OnProgressListener {

        fun onProgress(progress: Long, all: Long)
    }
}

inline fun File.new(): Boolean {
    return (parentFile?.mkdirs() ?: true) && delete() && createNewFile()
}

class FileTask : ITask {
    override val key: Key = Key("FileTask".toInt() * 1000 + FileTaskKeyHelper.curr)

    override suspend fun run(input: Data?): Result {

        return Result.failure()
    }
}

internal object FileTaskKeyHelper : IInterHelper by InterHelper()