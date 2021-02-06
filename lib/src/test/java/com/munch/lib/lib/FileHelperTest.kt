package com.munch.lib.lib

import com.munch.lib.helper.FileHelper
import com.munch.lib.helper.copyTo
import com.munch.lib.helper.md5Str
import com.munch.lib.helper.newDir
import org.junit.Test
import java.io.File

/**
 * Create by munch1182 on 2021/2/6 11:09.
 */
class FileHelperTest {

    @Test
    fun copy() {
        //将当前模块的src文件夹内容复制到dest文件夹
        assert(File("./src").copyTo(File("./dest").newDir(), copyDir = true))
    }

    @Test
    fun zip() {
        assert(
            FileHelper.zip(
                File("./src.zip"), null,
                true, *File("./src/main").listFiles()!!
            ) != null
        )
        assert(
            FileHelper.zip(
                File("./src2.zip"), null,
                false, *File("./src/main").listFiles()!!
            ) != null
        )
    }

    @Test
    fun unzip() {
        assert(FileHelper.unzip(File("./src.zip"), File("./unzip")))
    }

    @Test
    fun md5() {
        val md5Str = File("./build.gradle").md5Str()
        println(md5Str)
        assert(md5Str.isNotEmpty())
    }
}