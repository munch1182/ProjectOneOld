package com.munch.project.one.test

import android.annotation.SuppressLint
import android.os.Bundle
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.helper.TimeHelper
import com.munch.lib.helper.checkOrNew
import com.munch.lib.helper.closeQuietly
import com.munch.lib.log.Log2FileHelper
import com.munch.project.one.databinding.ActivityMappedByteBufferBinding
import java.io.Closeable
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

/**
 * Create by munch1182 on 2021/10/26 14:23.
 */
@SuppressLint("SetTextI18n")
class MappedByteBufferActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityMappedByteBufferBinding>()
    private val mmbHelper by lazy { Log2FileHelper(File(cacheDir, "log")) }
    private val ioHelper by lazy { Log2FileByIOHelper(File(cacheDir, "log2")) }

    private val count = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.mbbTestTitle.text = "写入${count}次数据耗时"
        bind.mbbTestMmb.setOnClickListener { testMMBWrite() }
        bind.mbbTestIo.setOnClickListener { testIOWrite() }
    }

    private fun testIOWrite() {
        thread {
            val cost = measureTimeMillis { repeat(count) { ioHelper.write("log:$it\n") } }
            bind.mbbTvIo.post { bind.mbbTvIo.text = "${bind.mbbTvIo.text}\ncost:$cost" }
            ioHelper.closeQuietly()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun testMMBWrite() {
        thread {
            val cost = measureTimeMillis { repeat(count) { mmbHelper.writeStr("log:$it\n") } }
            bind.mbbTvMmb.post { bind.mbbTvMmb.text = "${bind.mbbTvMmb.text}\ncost:$cost" }
            mmbHelper.closeQuietly()
        }
    }
}

class Log2FileByIOHelper(private val dir: File, private val newFile: (dir: File) -> File = byTime) :
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
                    val oneDay = TimeHelper.isOneDay(it.lastModified(), time)
                    oneDay && isLengthEnough(it)
                }
            todayFile ?: File(d, "${time}.txt")
        }

        fun isLengthEnough(file: File) = file.length() < SIZE_ONE_FILE - 100
    }


    init {
        dir.checkOrNew(false) ?: throw IOException("cannot get $dir")
    }

    private var pwNow: FileWriter? = null
    private var fileNow: File? = null

    private val pw: FileWriter
        get() = getNowPw()

    private fun getNowPw(): FileWriter {
        if (pwNow != null) {
            return pwNow!!
        }
        val f = fileNow?.takeIf { isLengthEnough(it) }
            ?: newFile.invoke(dir).apply { fileNow = this }
        return FileWriter(f, true).apply { pwNow = this }
    }


    fun write(any: String) {
        val position = fileNow?.length() ?: 0
        if (position + any.length > SIZE_ONE_FILE) {
            pwNow?.flush()
            pwNow?.closeQuietly()
            pwNow = null
        }
        pw.write(any)
        pw.flush()
    }

    override fun close() {
        pwNow?.closeQuietly()
    }
}