@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.pre.lib.helper.file

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.munch.pre.lib.DefaultDepend
import com.munch.pre.lib.base.BaseApp
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.channels.FileChannel
import java.security.MessageDigest
import java.text.DecimalFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * 与文件相关都要注意权限
 *
 * Create by munch1182 on 2021/1/1 1:05.
 */
@DefaultDepend([BaseApp::class])
object FileHelper {

    /**
     * 如果要新建缓存文件，优先使用该目录，以方便计算大小和清空缓存
     */
    fun cacheFileOrNew(context: Context = BaseApp.getInstance(), name: String): File? {
        return File(context.cacheDir, name).checkOrNew()
    }

    /**
     * 解析uri，包括文件转换的uri或者[toUriCompat]转换后的uri，将其转为file
     *
     * 注意：如果是content形式的uri，是采用复制的形式，以保持统一性，避开对各种后缀的查询，对于只是获取的操作来说足够了
     * 因此此方法无法对源文件进行操作，返回的file也不一定时源文件
     *
     * 一般的图片文件无需放入子线程操作，但可以考虑放入协程中进行
     *
     * @param file 用于复制的文件位置，如果目标文件是content的uri，该uri会被复制到该文件，如果确定是文件类型的uri，可以不传
     * @return 复制成功则返回文件，否则为null
     *
     * @see getPathFromUri
     */
    fun getFileFromUri(
        context: Context = BaseApp.getInstance(),
        uri: Uri,
        file: File? = null
    ): File? {
        when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> {
                return uri.toFile()
            }
            ContentResolver.SCHEME_CONTENT -> {
                file ?: return null
                val fileCreated = file.newFile() ?: return null
                try {
                    val ins = context.contentResolver.openInputStream(uri) ?: return null
                    val out = FileOutputStream(fileCreated)
                    if (ins.copyAndClose(out)) {
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
    const val STR_TB = "TB"

    const val KB = 1024L
    const val MB = KB * KB
    const val GB = KB * MB
    const val TB = KB * GB

    fun checkUnzipDirSize(zipFile: File): Int {
        return ZipFile(zipFile).size()
    }

    /**
     * 解压文件，此方法无法解压需要密码的压缩包
     *
     * 注意解压文件夹是否有足够的空间
     *
     * @param zipFile 需要解压的压缩包文件
     * @param dir 解压的文件夹位置，压缩包的文件会被放在该目录下
     * @param unzipDir 解压时是否保留文件结果，为true时保留原结构，为false时所有文件都将放在dir下
     *
     * @return 是否解压成功
     *
     * @see zip
     */
    fun unzip(zipFile: File, dir: File, unzipDir: Boolean = true): Boolean {
        dir.isDirOrNew() ?: return false
        val zip = ZipFile(zipFile)
        var file: File
        var flag = true
        for (entry in zip.entries()) {
            file = File(dir, if (unzipDir) entry.name else getNameFromPath(entry.name))
            if (!entry.isDirectory) {
                if (file.newFile() == null) {
                    flag = false
                    break
                }
                if (!zip.getInputStream(entry).copyAndClose(file.outputStream(), MB.toInt())) {
                    flag = false
                }
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
     * @param zipFile 压缩文件，文件后缀应该为压缩文件后缀
     * @param comment 压缩评论
     * @param zipDir 是否保留文件夹，为true则保留，否则只将文件压缩进压缩包
     * @param file 被压缩的文件，如果是文件夹，则该文件夹下的文件都会被压缩进压缩包
     *
     * @see unzip
     */
    fun zip(zipFile: File, comment: String?, zipDir: Boolean = true, vararg file: File): File? {
        zipFile.checkOrNew() ?: return null
        val zos = ZipOutputStream(zipFile.outputStream())
        zos.setComment(comment)
        val res = zip(zos, null, zipDir, file)
        zos.closeQuietly()
        return if (res) zipFile else null
    }

    private fun zip(
        zos: ZipOutputStream,
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
                            zos.putNextEntry(ZipEntry(("$base/")))
                            zos.closeEntry()
                        }
                    } else {
                        zip(zos, name, zipDir, it.listFiles()!!)
                    }
                } else {
                    zos.putNextEntry(ZipEntry(name))

                    var fis: FileInputStream? = null
                    try {
                        fis = FileInputStream(it)
                        fis.copyTo(zos)
                    } catch (e: IOException) {
                        throw e
                    } finally {
                        fis?.closeQuietly()
                        zos.closeEntry()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun getNameFromPath(path: String): String {
        if (!path.contains(File.separator)) {
            return path
        }
        return path.split(File.separator).last()
    }

    /**
     * 通过uri获取真实路径，可能会随版本变更而失效或者报错
     *
     * @see getFileFromUri
     */
    fun getPathFromUri(context: Context = BaseApp.getInstance(), uri: Uri): String? {
        return getRealPathFromUriAboveApi19(context, uri)
    }

    /**
     * 适配api19以上,根据uri获取图片的绝对路径
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
                val type = split[0]
                val contentUri: Uri
                contentUri = when (type) {
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
                uri, projection, selection, selectionArgs,
                null
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

/**
 * 检查文件是否存在，不存在则新建，新建失败则返回null，不会抛出异常
 */
fun File.checkOrNew(): File? {
    return this.takeIf { it.exists() } ?: newFile()
}

/**
 * 判断一个给定的文件夹文件是否存在且是否是文件夹，不存在则新建，新建失败或者不为文件夹则返回null，否则返回其本身
 */
fun File.isDirOrNew(): File? {
    return this.takeIf {
        it.mkdirs()
        it.exists() && it.isDirectory
    }
}

/**
 * 新建dir并返回其本身，主要用于链式调用
 */
fun File.newDir(): File {
    mkdirs()
    return this
}

/**
 * 新建一个文件，如果文件存在，则删除并重建，新建失败则返回null
 *
 * @see checkOrNew
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
    } catch (e: IOException) {
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
        e.printStackTrace()
        false
    }
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

@Deprecated(message = "无法简单的返回多层错误信息并处理，且逻辑简单，因此建议根据业务自行处理判断")
fun File.moveTo(file: File) {
    this.copyTo(file, true, copyDir = true)
    this.deleteFilesIgnoreRes()
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
    if (!dest.exists() && !dest.mkdirs()) {
        //创建失败则返回
        return false
    }
    //复制文件夹dest必须是文件夹
    if (!dest.isDirectory) {
        return false
    }

    if (this.isDirectory) {
        this.listFiles()?.forEach {
            if (it.isDirectory) {
                if (!it.copy2Dir(
                        if (copyDir) File(dest, it.name) else dest,
                        copyDir, preserveFileDate
                    )
                ) {
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
 * 复制当前文件到dest，参考org.apache.commons.io.FileUtils#doCopyFile
 *
 * @param dest 目标文件 如果不存在会被创建
 * @param preserveFileDate 是否同步文件修改时间
 * @return 是否复制成功
 *
 * @see copyTo
 */
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

/**
 * 返回16、32、24位的md5值，默认为16位
 *
 * win下查看文件md5命令: certutil -hashfile 文件 MD5
 */
fun File.md5Str(radix: Int = 16): String {
    val instance = MessageDigest.getInstance("MD5")
    FileInputStream(this).use {
        val buffer = ByteArray(FileHelper.MB.toInt())
        var len = it.read(buffer)
        while (len > 0) {
            instance.update(buffer, 0, len)
            len = it.read(buffer)
        }
    }
    return BigInteger(1, instance.digest()).toString(radix)
}

fun File.md5Check(md5: String, radix: Int = 16) = md5 == this.md5Str(radix)