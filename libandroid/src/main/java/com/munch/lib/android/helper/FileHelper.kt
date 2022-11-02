package com.munch.lib.android.helper

import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.catch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.max

/**
 * Create by munch1182 on 2022/10/14 11:27.
 */
object FileHelper {

    /**
     * 创建一个文件[size]大小的mmap
     *
     * 注意: 调用此方法会删除原有文件
     */
    fun mmap(file: File, size: Long = 4 * 1024L): MMAPHelper {
        if (file.exists()) file.delete() // 当前无法获取已生成文件的实际使用大小, 所以限定为新文件
        return MMAPHelper(file, size)
    }

    fun new(name: String) = File(AppHelper.cacheDir, name)

    /**
     * 将目标[dir]文件夹下的所有文件打包到[dest]文件中
     *
     * @param tile 如果文件夹下还有文件夹, 是否取消该文件结构, 平铺到压缩文件中
     *
     * @return 进度flow, SharedFlow为热流, 不需要collect也会执行
     */
    fun zip(dir: File, dest: File, tile: Boolean = false): SharedFlow<OnFileProgress> {
        val flow = MutableSharedFlow<OnFileProgress>()
        AppHelper.launch {
            withContext(Dispatchers.IO) {
                val progress = OnFileProgress.from(dir)
                delay(300L) // 等待flow先返回
                val zip = ZipOutputStream(FileOutputStream(dest))
                zipFileImp(zip, dir, "", tile) {
                    progress.progress(it.length())
                    progress.curr(1)
                    flow.emit(progress)
                }
                zip.finish()
                zip.close()
            }
        }
        return flow
    }

    private suspend fun zipFileImp(
        zip: ZipOutputStream,
        file: File,
        dir: String,
        tile: Boolean,
        change: (suspend (File) -> Unit)? = null
    ): Boolean {
        return withContext(Dispatchers.IO) {
            catch {
                if (file.isFile) {
                    val entry = ZipEntry(if (tile) file.name else "$dir/${file.name}")
                    zip.putNextEntry(entry)
                    FileInputStream(file).use { zip.write(it.readBytes()) }
                    zip.closeEntry()
                    change?.invoke(file)
                } else if (file.isDirectory) {
                    val files = file.listFiles()
                    if ((files == null || files.isEmpty()) && !tile) { // 空文件夹, 非平铺时仍保留
                        val entry = ZipEntry("$dir/${file.name}")
                        zip.putNextEntry(entry)
                        zip.closeEntry()
                    } else {
                        files?.forEach { zipFileImp(zip, it, "$dir/${file.name}", tile, change) }
                    }
                }
                true
            } ?: false
        }
    }

    /**
     * 判断在[dir]文件夹下[name]是否已存在, 如果已存在, 则添加后缀[add]以生成新名称, 如此循环直到该名称可用
     */
    suspend fun newFileName(dir: File, name: String, add: String = "(1)"): String {
        return withContext(Dispatchers.IO) {
            if (dir.isFile) {
                if (dir.name == name) return@withContext "$name$add"
            } else if (dir.isDirectory) {
                if (dir.list()?.find { it != name } != null) {
                    newFileName(dir, "$name$add") // 在后面无限加(1)
                }
            }
            name
        }
    }

    /**
     * 文件夹内及其子文件夹的总文件数量(不包括文件夹)
     */
    inline val File.count: Int
        get() = count(this, 0)

    /**
     * 文件夹及其子文件的总文件大小(文件夹的实际大小需要用此方法)
     */
    inline val File.size: Long
        get() = size(this, 0)

    fun size(file: File, size: Long = 0): Long {
        return if (file.isFile) {
            size + file.length()
        } else if (file.isDirectory) {
            var curr = size
            file.listFiles()?.forEach { curr = size(it, curr) }
            curr
        } else {
            size
        }
    }

    fun count(file: File, count: Int = 0): Int {
        return if (file.isFile) {
            count + 1
        } else if (file.isDirectory) {
            var curr = count
            file.listFiles()?.forEach { curr = count(it, curr) }
            curr
        } else {
            count
        }
    }

    /**
     * 文件大小的进度
     */
    data class OnSizeProgress(val total: Long, private var _progress: Long = 0L) {
        val progress: Long
            get() = _progress

        fun progress(progress: Long): OnSizeProgress {
            this._progress += progress
            return this
        }

        override fun toString() = "($_progress/$total)"
    }

    /**
     * 第几步的进度
     */
    data class OnStepProgress(val all: Int, private var _curr: Int = 0) {
        val curr: Int
            get() = _curr

        fun curr(curr: Int): OnStepProgress {
            this._curr += curr
            return this
        }

        override fun toString() = "($_curr/$all)"
    }

    data class OnFileProgress(val size: OnSizeProgress, val step: OnStepProgress) {

        companion object {
            fun from(file: File): OnFileProgress {
                return OnFileProgress(file.size, file.count)
            }
        }

        constructor(total: Long, all: Int) : this(OnSizeProgress(total), OnStepProgress(all))

        fun progress(progress: Long): OnFileProgress {
            this.size.progress(progress)
            return this
        }

        fun curr(curr: Int): OnFileProgress {
            this.step.curr(curr)
            return this
        }

        override fun toString(): String {
            return "(${size.progress}/${size.total}, ${step.curr}/${step.all})"
        }
    }
}

class MMAPHelper internal constructor(file: File, size: Long) : File(file.absolutePath), Closeable {

    private val channel = RandomAccessFile(file, "rw").channel
    private var mmap: MappedByteBuffer? = newMMAP(size)

    suspend fun write(str: String) = write(str.toByteArray())

    suspend fun write(bytes: ByteArray) {
        val m = mmap ?: return
        if (m.position() + bytes.size > getFileSize()) { // 如果剩余空间不够, 则需要扩容
            grow(bytes)
            return
        }
        m.put(bytes) //会自动更改m.position()的位置
    }

    private suspend fun grow(bytes: ByteArray) {
        val position = mmap?.position() ?: 0 // 上一次写入的位置
        val fileSize = getFileSize()
        val size = max(fileSize * 2, fileSize + bytes.size) // 扩容
        close() // 销毁当前mmap对象
        mmap = newMMAP(size) // 新建一个范围更大的mmap对象
        mmap?.position(position) // 移动到上一次结束的位置
        write(bytes) // 扩容之后再次尝试写入
    }

    private suspend fun getFileSize(): Long {
        return withContext(Dispatchers.IO) { channel.size() }
    }

    private fun newMMAP(size: Long) = channel.map(FileChannel.MapMode.READ_WRITE, 0, size)

    override fun close() {
        mmap = null
    }
}

