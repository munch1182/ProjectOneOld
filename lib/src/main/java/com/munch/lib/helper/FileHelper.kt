package com.munch.lib.helper

import android.content.ContentResolver
import android.content.Context
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

    /**
     * 如果要新建缓存文件，优先使用该目录，以方便计算大小和清空缓存
     */
    fun newCacheFile(context: Context = BaseApp.getInstance(), name: String): File? {
        return createFile(File(context.cacheDir, name))
    }

    fun createFile(file: File): File? {
        file.mkdirs()
        return try {
            if (file.exists()) {
                file.delete()
            }
            if (file.createNewFile()) {
                file
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Throws(IOException::class, SecurityException::class)
    fun deleteFile(file: File?) {
        if (file?.exists() != true) {
            return
        }
        if (file.isFile) {
            file.delete()
        } else {
            file.listFiles()?.forEach {
                deleteFile(it)
            }
            file.delete()
        }
    }

    fun deleteFileIgnoreException(file: File?) {
        try {
            deleteFile(file)
        } catch (e: Exception) {
            //DO NOTHING
        }
    }

    fun getFileSize(file: File?): Long {
        file?.takeIf { f -> f.exists() } ?: return 0L
        var size = 0L
        if (!file.isDirectory) {
            size = file.length()
        } else {
            file.listFiles()?.forEach {
                size += getFileSize(it)
            }
        }
        return size
    }

    fun formatSizeCache(context: Context) = formatSize2Str(context.cacheDir.length().toDouble())

    fun formatSize2Str(size: Double) = formatSize(size).run { "$first$second" }

    /**
     * @return <数值，单位> 如<"5","KB">
     */
    fun formatSize(size: Double): Pair<String, String> {
        if (size == 0.0) {
            return Pair("0", "B")
        }
        val df = DecimalFormat("#.00")
        return when {
            size < 1024.0 -> {
                Pair(df.format(size), "B")
            }
            size < 1048576.0 -> {
                Pair(df.format(size / 1024.0), "KB")
            }
            size < 1073741824.0 -> {
                Pair(df.format(size / 1048576.0), "MB")
            }
            else -> {
                Pair(df.format(size / 1073741824.0), "GB")
            }
        }
    }

    /**
     * 版本高于24需要在manifest中声明provider
     * 且声明的provider的authority需要与传入的[authority]一致
     *
     * @see <a>https://developer.android.google.cn/reference/kotlin/androidx/core/content/FileProvider</a>
     */
    fun getUri(
        context: Context = BaseApp.getInstance(),
        file: File,
        authority: String = "${context.packageName}.fileProvider"
    ): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, authority, file)
        } else {
            Uri.fromFile(file)
        }
    }

    /**
     * 解析uri，包括文件转换的uri或者[getUri]转换后的uri，将其转为file
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
                val fileCreated = createFile(file) ?: return null
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

    /**
     * 获取文件后缀
     */
    fun getExtension(file: File): String? = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.toUri().toString()))
}