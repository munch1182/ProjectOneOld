@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.munch.lib.result.ResultHelper
import com.munch.lib.result.start
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.coroutines.resume

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
    fun toUri(
        context: Context,
        file: File,
        authority: String = "${context.packageName}.fileprovider"
    ): Uri? {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, authority, file)
        } else {
            Uri.fromFile(file)
        }
    }

    /**
     * 将uri的数据复制到文件中
     *
     * @param f 如果传入的是个文件夹，则将uri复制到该文件夹下同名文件，否则，则复制到该文件中
     */
    fun uri2File(
        context: Context,
        uri: Uri?,
        f: File = context.cacheDir,
        onProgress: OnProgressListener? = null
    ): File? {
        uri ?: return null
        val file = if (f.isFile) f else queryName(context, uri)?.let { File(f, it) } ?: return null
        return context.contentResolver.openInputStream(uri)?.close {
            if (!copy2File(it, file, false, onProgress = onProgress)) {
                return null
            }
            file
        }
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
        if (!file.new(false)) return false
        return FileOutputStream(file, append).close { os ->
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
        } ?: false
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

    suspend fun chose(activity: FragmentActivity, type: String = "*/*"): Uri? {
        return suspendCancellableCoroutine {
            ResultHelper.with(activity)
                .intent(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
                    setType(type)
                    addCategory(Intent.CATEGORY_OPENABLE)
                }, "chose"))
                .start { isOk, data ->
                    it.resume(if (isOk) data?.data else null)
                }
        }
    }
}

/**
 * 创建文件
 *
 * @param delIfExists 如果存在该文件，是否将其删除，此次没有判断存在同名文件夹的情形
 */
fun File.new(delIfExists: Boolean = true): Boolean {
    return (parentFile?.let { it.exists() or it.mkdirs() } ?: true)
            && (if (delIfExists && exists()) delete() else true)
            && createNewFile()
}

/**
 * 删除所有能删除的文件或者文件夹内的文件及文件本身
 */
fun File.del(): Boolean {
    if (isFile) return delete()
    var result = true
    listFiles()?.forEach {
        result = it.del() && result
    }
    delete()
    return result
}

inline fun <T : Closeable?, R> T.close(block: (T) -> R): R? {
    return try {
        block(this)
    } catch (e: Throwable) {
        null
    } finally {
        when {
            this == null -> {}
            else ->
                try {
                    close()
                } catch (closeException: Throwable) {
                }
        }
    }
}