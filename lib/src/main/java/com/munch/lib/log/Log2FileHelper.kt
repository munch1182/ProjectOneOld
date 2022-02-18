package com.munch.lib.log

import com.munch.lib.helper.TimeHelper
import com.munch.lib.helper.checkOrNew
import com.munch.lib.helper.read
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Create by munch1182 on 2021/10/27 17:01.
 */
/**
 * @param dir 用于保存日志文件的文件夹
 * @param newFile 生成新文件的方式，需要在该dir中判断是否需要继写在该文件，如果要依据事件或者状态分类文件，可以设置此方法
 */
class Log2FileHelper(private val dir: File, private val newFile: (dir: File) -> File = byTime) :
    Closeable {

    companion object {
        private const val SIZE_ONE_FILE = 40 * 1024 //40kb

        /**
         * 根据文件时间和大小作为创建新文件的依据
         */
        val byTime: (dir: File) -> File = { d ->
            val time = System.currentTimeMillis()
            val todayFile: File? = d.listFiles()
                ?.find {
                    TimeHelper.isOneDay(it.lastModified(), time) && isLengthEnough(it)
                }
            todayFile ?: File(d, "${time}.txt")
        }

        fun isLengthEnough(file: File): Boolean {
            val i = ByteBuffer.wrap(file.read(4) ?: return false).getInt(0)
            return i < SIZE_ONE_FILE - 100
        }
    }

    init {
        dir.checkOrNew(false) ?: throw IOException("cannot get $dir")
    }

    private var mbbNow: MappedByteBuffer? = null
    private var fileNow: File? = null

    private val mbb: MappedByteBuffer
        get() = getCurrentMbb()

    fun write(any: Any?, end: Boolean = true) {
        writeStr(FMT.any2Str(any), end)
    }

    fun writeStr(str: String, end: Boolean = true) {
        val position = mbb.position()
        if (position + str.length > SIZE_ONE_FILE) {
            mbbNow = null
        }
        mbb.apply {
            put(str.toByteArray())
            putInt(0, position())
            if (end) {
                put("\r\n".toByteArray())
            }
        }
    }

    val currentFile: File?
        get() = fileNow

    private fun getCurrentMbb(): MappedByteBuffer {
        return mbbNow ?: newMBB()
    }

    private fun newMBB(): MappedByteBuffer {
        //预留100字节
        val f = fileNow?.takeIf { isLengthEnough(it) }
            ?: newLogFile().apply { fileNow = this }
        return RandomAccessFile(f, "rw")
            .channel
            .map(FileChannel.MapMode.READ_WRITE, 0, SIZE_ONE_FILE.toLong()).apply {
                val i = getInt(0)
                if (i != 0) {
                    position(i)
                } else {
                    putInt(0)
                    put("\n".toByteArray())
                }
                mbbNow = this
            }
    }

    private fun newLogFile() = newFile.invoke(dir)

    override fun close() {
        mbbNow = null
    }
}