package com.munch.lib.helper

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.munch.lib.app.AppHelper
import java.io.*
import java.math.BigInteger
import java.nio.channels.FileChannel
import java.security.MessageDigest
import java.text.DecimalFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Create by munch1182 on 2021/8/17 10:01.
 */

/**
 * 关闭流，不会抛出异常
 */
fun Closeable?.closeQuietly() {
    try {
        this?.close()
    } catch (e: IOException) {
        //ignore
    }
}

/**
 * 从一个文件中从[start]开始读取[length]长度的内容
 *
 * 如果发生IO异常，或者长度不够，则返回null
 */
fun File.read(length: Int, start: Int = 0): ByteArray? {
    var byteArray: ByteArray?
    var fis: FileInputStream? = null
    try {
        fis = FileInputStream(this)
        byteArray = ByteArray(length)
        if (fis.read(byteArray, start, length) != length) {
            byteArray = null
        }
    } catch (e: IOException) {
        byteArray = null
    } finally {
        fis?.closeQuietly()
    }
    return byteArray
}

/**
 * 确保该文件存在且无内容/确保该文件夹存在且为空文件夹
 *
 * 即，如果该文件不存在，则新建；如果该文件已存在，则删除并新建
 *
 * @param isFile 是否是一个文件，否则是一个文件夹
 *
 * @return 如果成功，返回该文件或者文件夹，否则返回null
 */
fun File.sureNew(isFile: Boolean = true): File? {
    val isSuccess = if (isFile) {
        //file
        (if (exists() && this.isFile) delete() else true)
                //因为mkdirs会在已存在时返回false
                && (if (parentFile?.exists() == false) (parentFile?.mkdirs() ?: true) else true)
                && createNewFile()
        //dir
    } else {
        (if (exists() && this.isDirectory) del() else true) && mkdirs()
    }
    return if (isSuccess) this else null
}

fun File.sureFile() = sureNew(true)
fun File.sureDir() = sureNew(false)

/**
 * 检查文件是否存在，不存在则新建，新建失败则返回null
 *
 *  @param isFile 是否是一个文件，否则是一个文件夹
 */
fun File.checkOrNew(isFile: Boolean = true): File? {
    return this.takeIf { it.exists() } ?: sureNew(isFile)
}

/**
 * 删除该文件/删除该文件夹及其中的所有内容
 */
fun File.del(): Boolean {
    var isSuccess = true
    if (isFile) {
        isSuccess = if (exists()) delete() else true
    } else {
        listFiles()?.forEach { isSuccess = isSuccess && it.del() }
        delete()
    }
    return isSuccess
}

/**
 * 获取文件或者文件夹的大小
 */
fun File.getSize(): Long {
    if (!exists()) {
        return 0L
    }
    var size = 0L
    if (isFile) {
        size = length()
    } else {
        listFiles()?.forEach { size += it.getSize() }
    }
    return size
}

/**
 * 文件转为uri
 *
 * 版本高于24需要在manifest中声明provider
 * 且声明的provider的authority需要与传入的[authority]一致
 *
 * @see <a>https://developer.android.google.cn/reference/kotlin/androidx/core/content/FileProvider</a>
 */
fun File.toUriCompat(
    context: Context = AppHelper.app,
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

/**
 * 返回该文件的md5值，默认为16位
 *
 * win下查看文件md5命令: certutil -hashfile 文件 MD5
 *
 * @param radix md5值位数，可选16,24,32
 */
@WorkerThread
fun File.md5Str(radix: Int = 16): String? {
    val instance = MessageDigest.getInstance("MD5")
    var fis: FileInputStream? = null
    try {
        fis = FileInputStream(this)
        val buffer = ByteArray(FileHelper.MB.toInt())
        var len = fis.read(buffer)
        while (len > 0) {
            instance.update(buffer, 0, len)
            len = fis.read(buffer)
        }
    } catch (e: IOException) {
        return null
    } finally {
        fis?.closeQuietly()
    }
    return BigInteger(1, instance.digest()).toString(radix)
}

fun File.md5Check(md5: String, radix: Int = 16) = md5 == this.md5Str(radix)

/**
 * 复制当前文件到dest，参考org.apache.commons.io.FileUtils#doCopyFile
 *
 * @param dest 目标文件 如果不存在会被创建
 * @param preserveFileDate 是否同步文件修改时间
 * @return 是否复制成功
 *
 * @see copyTo
 */
@WorkerThread
fun File.copy2File(
    dest: File,
    preserveFileDate: Boolean = true,
    bufferSize: Long = 5L * FileHelper.MB
): Boolean {
    dest.checkOrNew() ?: return false
    var fis: FileInputStream? = null
    var fos: FileOutputStream? = null
    var input: FileChannel? = null
    var output: FileChannel? = null
    try {
        fis = FileInputStream(this)
        fos = FileOutputStream(dest)
        input = fis.channel
        output = fos.channel
        val size = input.size()
        var pos: Long = 0
        var count: Long
        while (pos < size) {
            count = if (size - pos > bufferSize) bufferSize else size - pos
            pos += output.transferFrom(input, pos, count)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return false
    } finally {
        output?.closeQuietly()
        fos?.closeQuietly()
        input?.closeQuietly()
        fis?.closeQuietly()
    }

    if (this.length() != dest.length()) {
        return false
    }
    if (preserveFileDate) {
        dest.setLastModified(this.lastModified())
    }
    return true
}

/**
 * 复制当前文件或者文件夹到目标文件夹
 *
 * @param dest 目标文件夹，不能是文件
 * @param copyDir 是否复制文件夹，为false时复制当前文件夹下所有文件到dest一级目录下，否则会保持当前文件夹结构，当前是文件则该参数无效
 * @param preserveFileDate 是否同步文件修改时间
 * @return 是否复制成功
 *
 * @see copyTo
 */
fun File.copy2Dir(
    dest: File,
    preserveFileDate: Boolean = true,
    copyDir: Boolean = true
): Boolean {
    //不存在则创建
    dest.checkOrNew(true) ?: return false
    //复制文件夹dest必须是文件夹
    if (!dest.isDirectory) {
        return false
    }

    if (this.isDirectory) {
        this.listFiles()?.forEach {
            if (it.isDirectory) {
                val newDest = if (copyDir) File(dest, it.name) else dest
                if (!it.copy2Dir(newDest, copyDir, preserveFileDate)) {
                    return false
                }
            } else {
                if (!it.copy2File(File(dest, it.name), preserveFileDate)) {
                    return false
                }
            }
        }
    } else {
        return this.copy2File(File(dest, this.name), preserveFileDate)
    }
    if (preserveFileDate) {
        dest.setLastModified(this.lastModified())
    }
    return true
}

/**
 * 复制当前文件或者文件夹到目标文件或者文件夹
 *
 * @param dest 目标文件或者文件夹，当dest是文件时，调用的文件不能是文件夹
 * @param preserveFileDate 是否同步文件修改时间
 * @param copyDir 复制时是否保留文件夹结构，仅当dest是文件夹是有效
 *
 * 不能将文件夹复制到文件
 */
fun File.copyTo(
    dest: File,
    preserveFileDate: Boolean = true,
    copyDir: Boolean = true
): Boolean {
    if (dest.isDirectory) {
        return this.copy2Dir(dest, preserveFileDate, copyDir)
    } else if (this.isFile) {
        return this.copy2File(dest, preserveFileDate)
    }
    throw UnsupportedOperationException("cannot copy dir to file")
}

/**
 * 从路径中获取文件名(包括后缀)
 */
fun getNameFromPath(path: String): String {
    if (!path.contains(File.separator)) {
        return path
    }
    return path.split(File.separator).last()
}

/**
 * 解压文件，此方法无法解压需要密码的压缩包
 *
 * @param dir 解压的文件夹位置，压缩包的文件会被放在该目录下
 * @param unzipDir 解压时是否保留文件结果，为true时保留原结构，为false时所有文件都将放在dir下
 *
 * @return 是否解压成功
 */
fun File.unzip(dir: File, unzipDir: Boolean = true): Boolean {
    val dirNew = dir.sureNew(false) ?: return false
    if (!dirNew.isDirectory) {
        return false
    }
    val zip = ZipFile(this)
    var file: File
    var flag = true
    for (entry in zip.entries()) {
        file = File(dirNew, if (unzipDir) entry.name else getNameFromPath(entry.name))
        if (!entry.isDirectory) {
            if (file.sureNew() == null) {
                flag = false
                break
            }
            val fis = zip.getInputStream(entry)
            val fos = file.outputStream()
            fis.copyTo(fos)
            fis.closeQuietly()
            fos.closeQuietly()
        } else if (unzipDir) {
            file.mkdirs()
        }
    }
    zip.closeQuietly()
    return flag
}


/**
 * 压缩文件，此方法无法添加压缩密码
 *
 * @param comment 压缩评论
 * @param zipDir 是否保留文件夹，为true则保留，否则只将文件压缩进压缩包
 * @param file 被压缩的文件，如果是文件夹，则该文件夹下的文件都会被压缩进压缩包
 *
 * @see unzip
 */
fun File.zip(comment: String?, zipDir: Boolean = true, vararg file: File): File? {
    this.checkOrNew() ?: return null
    val zos = ZipOutputStream(this.outputStream())
    zos.setComment(comment)
    val res = zos.zip(null, zipDir, file)
    zos.closeQuietly()
    return if (res) this else null
}

private fun ZipOutputStream.zip(
    base: String?,
    zipDir: Boolean = true,
    files: Array<out File>
): Boolean {
    try {
        files.forEach {
            val name = if (base == null || !zipDir) it.name else "$base/${it.name}"
            if (it.isDirectory) {
                if (it.list().isNullOrEmpty()) {
                    if (zipDir) {
                        //空文件夹处理
                        putNextEntry(ZipEntry(("$base/")))
                        closeEntry()
                    }
                } else {
                    zip(name, zipDir, it.listFiles()!!)
                }
            } else {
                putNextEntry(ZipEntry(name))

                var fis: FileInputStream? = null
                try {
                    fis = FileInputStream(it)
                    fis.copyTo(this)
                } catch (e: IOException) {
                    throw e
                } finally {
                    fis?.closeQuietly()
                    closeEntry()
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return false
    }
    return true
}

object FileHelper {

    const val KB = 1024L
    const val MB = 1024L * KB
    const val GB = 1024L * MB
    const val TB = 1024L * GB

    const val STR_BITE = "B"
    const val STR_KB = "KB"
    const val STR_MB = "MB"
    const val STR_GB = "GB"
    const val STR_TB = "TB"


    private val app = AppHelper.app

    /**
     * 新建一个缓存文件夹内的新文件
     *
     * @see sureNew
     */
    fun newCache(name: String, context: Context = app) = File(context.cacheDir, name).sureNew()

    /**
     * 解析uri，包括文件转换的uri或者[toUriCompat]转换后的uri，将其转为file
     *
     * 注意：如果是content形式的uri，是采用复制的形式，以保持统一性，避开对各种后缀的查询，对于只是获取的操作来说足够了
     * 因此此方法无法对源文件进行操作，返回的file也不一定是源文件
     *
     * @param file 用于复制的文件位置，如果目标文件是content的uri，该uri会被复制到该文件，如果确定是文件类型的uri，可以不传
     * @return 复制成功则返回文件，否则为null
     *
     */
    fun getFileFromUri(
        context: Context = app,
        uri: Uri,
        file: File? = null
    ): File? {
        when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> {
                return uri.toFile()
            }
            ContentResolver.SCHEME_CONTENT -> {
                file ?: return null
                val fileCreated = file.sureNew() ?: return null
                var ins: InputStream? = null
                var out: FileOutputStream? = null
                try {
                    ins = context.contentResolver.openInputStream(uri) ?: return null
                    out = FileOutputStream(fileCreated)
                    ins.copyTo(out)
                    return fileCreated
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                } finally {
                    ins?.closeQuietly()
                    out?.closeQuietly()
                }
            }
        }
        return null
    }

    /**
     * 将[size]转为其值区间的值和单位
     *
     * 如: 1025 -> 1.1 KB
     *
     * @see [STR_BITE] [STR_KB] [STR_MB] [STR_GB]
     * @return <数值，单位> 如<"5","KB">
     */
    fun formatSize(size: Double): Pair<String, String> {
        if (size == 0.0) {
            return Pair("0", STR_BITE)
        }
        val df = DecimalFormat("#.00")
        return when {
            size < 1024.0 -> Pair(df.format(size), STR_BITE)
            size < 1048576.0 -> Pair(df.format(size / 1024.0), STR_KB)
            size < 1073741824.0 -> Pair(df.format(size / 1048576.0), STR_MB)
            else -> Pair(df.format(size / 1073741824.0), STR_GB)
        }
    }

    /**
     * 采用系统的方法格式化文件大小
     */
    fun formatSizeStr(context: Context? = app, size: Long) =
        Formatter.formatFileSize(context, size)

    /**
     * 返回文件大小的字符串
     */
    fun formatSize2Str(size: Double) = formatSize(size).run { "$first$second" }

    /**
     * 通过uri获取真实路径，可能会随版本变更而失效或者报错
     *
     * @see getFileFromUri
     */
    fun getPathFromUri(context: Context = app, uri: Uri): String? {
        return getRealPathFromUriAboveApi19(context, uri)
    }

    /**
     * 适配api19以上,根据uri获取绝对路径
     */
    @Suppress("DEPRECATION")
    private fun getRealPathFromUriAboveApi19(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong()
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val contentUri: Uri = when (split[0]) {
                    "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    else -> MediaStore.Files.getContentUri("external")
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    @Suppress("DEPRECATION")
    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        val column = MediaStore.MediaColumns.DATA
        val projection = arrayOf(column)
        try {
            context.contentResolver.query(
                uri, projection, selection, selectionArgs, null
            ).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(columnIndex)
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}