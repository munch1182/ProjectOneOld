package com.munch.lib.helper

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.munch.lib.BaseApp
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat

/**
 * 与文件相关都要注意权限
 *
 * Create by munch1182 on 2021/1/1 1:05.
 */
object FileHelper {

    fun fileIntent(type: String = "*/*") =
        Intent(Intent.ACTION_GET_CONTENT).setType(type).addCategory(Intent.CATEGORY_OPENABLE)

    /**
     * 如果要新建缓存文件，优先使用该目录，以方便计算大小和清空缓存
     */
    fun newCacheFile(context: Context = BaseApp.getInstance(), name: String): File? {
        return File(context.cacheDir, name).newFile()
    }

    /**
     * 解析uri，包括文件转换的uri或者[toUriCompat]转换后的uri，将其转为file
     *
     * 注意：如果是content形式的uri，是采用复制的形式，以保持统一性，避开对各种后缀的查询，对于只是获取的操作来说足够了
     * 因此此方法无法对源文件进行操作
     *
     * 一般的图片文件无需放入子线程操作，但可以考虑放入协程中进行
     *
     * @param file 用于复制的文件，如果确定是文件类型的uri，可以不传
     */
    fun uri2File(context: Context = BaseApp.getInstance(), uri: Uri, file: File? = null): File? {
        when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> {
                return uri.toFile()
            }
            ContentResolver.SCHEME_CONTENT -> {
                file ?: return null
                val fileCreated = file.newFile() ?: return null
                try {
                    context.contentResolver.openInputStream(uri)?.run {
                        var out: FileOutputStream? = null
                        try {
                            out = FileOutputStream(fileCreated)
                            this.copyTo(out)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            this.close()
                            out?.close()
                        }
                        return fileCreated
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }


    fun formatSizeCache(context: Context) = formatSize2Str(context.cacheDir.length().toDouble())

    fun formatSize2Str(size: Double) = formatSize(size).run { "$first$second" }

    /**
     * @see [STR_BITE] [STR_KB] [STR_MB] [STR_GB]
     * @return <数值，单位> 如<"5","KB">
     */
    fun formatSize(size: Double): Pair<String, String> {
        if (size == 0.0) {
            return Pair("0", STR_BITE)
        }
        val df = DecimalFormat("#.00")
        return when {
            size < 1024.0 -> {
                Pair(df.format(size), STR_BITE)
            }
            size < 1048576.0 -> {
                Pair(df.format(size / 1024.0), STR_KB)
            }
            size < 1073741824.0 -> {
                Pair(df.format(size / 1048576.0), STR_MB)
            }
            else -> {
                Pair(df.format(size / 1073741824.0), STR_GB)
            }
        }
    }

    const val STR_BITE = "B"
    const val STR_KB = "KB"
    const val STR_MB = "MB"
    const val STR_GB = "GB"
}

/**
 * 检查文件是否存在，不存在则新建，新建失败则返回null
 */
fun File.checkOrNew(): File? {
    return this.takeIf { it.exists() } ?: newFile()
}

/**
 * 新建一个文件
 * 如果文件存在，则删除并重建，新建失败则返回null
 */
fun File.newFile(): File? {
    mkdirs()
    return try {
        if (exists()) {
            delete()
        }
        if (createNewFile()) {
            this
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 删除一个文件或者文件夹
 *
 * @return 全部删除成功
 */
@Throws(IOException::class, SecurityException::class)
fun File.deleteFiles(): Boolean {
    if (!exists()) {
        return true
    }
    var flag: Boolean
    if (isFile) {
        flag = delete()
    } else {
        flag = true
        listFiles()?.forEach {
            flag = flag && it.deleteFiles()
        }
        flag = flag && delete()
    }
    return flag
}

/**
 * 删除文件或者文件夹，并不处理结果
 */
fun File.deleteFilesIgnoreRes(): Boolean {
    return try {
        deleteFiles()
    } catch (e: Exception) {
        false
    }
}

/**
 * 获取文件或者文件夹的大小
 */
fun File.getSize(): Long {
    if (!exists()) {
        return 0L
    }
    var size = 0L
    if (!isDirectory) {
        size = length()
    } else {
        listFiles()?.forEach {
            size += it.getSize()
        }
    }
    return size
}

/**
 * 版本高于24需要在manifest中声明provider
 * 且声明的provider的authority需要与传入的[authority]一致
 *
 * @see <a>https://developer.android.google.cn/reference/kotlin/androidx/core/content/FileProvider</a>
 */
fun File.toUriCompat(
    context: Context = BaseApp.getInstance(),
    authority: String = "${context.packageName}.fileProvider"
): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(context, authority, this)
    } else {
        Uri.fromFile(this)
    }
}

/**
 * 获取文件后缀
 */
fun File.getExtension(): String? = MimeTypeMap.getSingleton()
    .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(this.toUri().toString()))