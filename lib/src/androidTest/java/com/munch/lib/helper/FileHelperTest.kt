package com.munch.lib.helper

import androidx.test.platform.app.InstrumentationRegistry
import com.munch.lib.log
import org.junit.Test

import java.io.File

/**
 * Create by yf-20-11-24-01 on 2021/3/23 10:42.
 */
class FileHelperTest {

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun uri2File() {
    }

    @Test
    fun checkUnzipDirSize() {
        val size = FileHelper.checkUnzipDirSize(File(appContext.cacheDir, "test.zip"))
        log(size,FileHelper.formatSize2Str(size.toDouble()))
    }

    @Test
    fun unzip1() {
        val newCacheFile = File(appContext.cacheDir, "/testunzip")
        newCacheFile.deleteFilesIgnoreRes()
        newCacheFile.mkdirs()
        val zipFile = File(appContext.cacheDir, "test.zip")
        FileHelper.unzip(zipFile, newCacheFile)
        assert(true)
    }

    @Test
    fun unzip2() {
        val newCacheFile = File(appContext.cacheDir, "/testunzip2")
        newCacheFile.deleteFilesIgnoreRes()
        newCacheFile.mkdirs()
        val zipFile = File(appContext.cacheDir, "test.zip")
        FileHelper.unzip(zipFile, newCacheFile, false)
        assert(true)
    }

    @Test
    fun zip() {
        val newCacheFile = File(appContext.cacheDir, "/test")
        newCacheFile.deleteFilesIgnoreRes()
        newCacheFile.newDir()
        for (i in 0..3) {
            val name = "test_$i"
            val file = File(newCacheFile, name)
            file.mkdirs()
            val newFile = File(file, "$name.txt").newFile()
            newFile?.writeText("$i")
        }
        val zipFile = FileHelper.cacheFileOrNew(appContext, "test.zip") ?: return
        FileHelper.zip(zipFile, null, file = newCacheFile.listFiles() ?: return)
        assert(true)
    }
}