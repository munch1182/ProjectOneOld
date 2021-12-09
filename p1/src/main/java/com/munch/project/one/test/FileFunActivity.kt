package com.munch.project.one.test

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.munch.lib.base.toBytes
import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.helper.FileHelper
import com.munch.lib.helper.del
import com.munch.lib.helper.lengthAll
import com.munch.lib.log.log
import com.munch.lib.result.ResultHelper
import com.munch.lib.result.hasPermission
import com.munch.lib.result.with
import com.munch.lib.state.ViewNoticeHelper
import com.munch.project.one.weight.NameLoadingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/8/17 10:36.
 */
class FileFunActivity : BaseBtnFlowActivity() {

    private var choseFile: Uri? = null
    private val noticeHelper by lazy {
        ViewNoticeHelper(this, loading = flowLayout to NameLoadingView(this))
    }

    override fun getData() =
        mutableListOf(
            "request permission",
            "chose file",
            "open file",
            "del file",
            "clear cache",
            "new file",
            "new char file",
            "compress",
            "decompress"
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val havePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            hasPermission(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        flowLayout.set { group = mutableListOf(1, 1, 2, 1, 2, 2).toTypedArray() }
        showNotice("是否有读写权限：$havePermission")
    }

    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            //request permission
            0 -> {
                val result: (result: Boolean) -> Unit =
                    { showNotice("申请权限${if (it) "成功" else "失败"}") }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    with(
                        { Environment.isExternalStorageManager() },
                        Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    ).start { result.invoke(it) }
                } else {
                    with(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ).request { result.invoke(it) }
                }
            }
            //chose file
            1 -> {
                with(Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }).start(object : ResultHelper.OnActivityResultListener {
                    override fun onResult(isOk: Boolean, resultCode: Int, data: Intent?) {
                        choseFile = data?.data
                        choseFile?.let {
                            showNotice("已选择文件: $it")
                        }
                    }
                })
            }
            //open file
            2 -> {
            }
            //del file
            3 -> {
            }
            //clear cache
            4 -> {
                noticeHelper.loading {
                    val size = FileHelper.formatSize(cacheDir.lengthAll().toDouble())
                    cacheDir.del()
                    withContext(Dispatchers.Main) { showNotice("缓存文件(${size.first}${size.second})已清除") }
                }
            }
            //new file
            5 -> newFile()
            6 -> newCharFile()
            //compress
            7 -> compress()
            //decompress
            8 -> decompress()
        }
    }

    private fun newCharFile() {
        @Suppress("BlockingMethodInNonBlockingContext")
        noticeHelper.loading {
            val f = FileHelper.newCache("test_compress_char")
            f?.let {
                val size = FileHelper.MB * 10
                val m = RandomAccessFile(it, "rw")
                    .channel
                    .map(FileChannel.MapMode.READ_WRITE, 0, size)
                var writeSize = 0
                val r = Random
                val charA = 'a'
                while (writeSize < size) {
                    val bytes = (charA + r.nextInt(26)).toBytes()
                    m.put(bytes)
                    writeSize += bytes.size
                }
                choseFile = f.toUri()
                runOnUiThread {
                    showNotice("文件已生成")
                }
            }
        }
    }

    private fun newFile() {
        @Suppress("BlockingMethodInNonBlockingContext")
        noticeHelper.loading {
            val f = FileHelper.newCache("test_compress")
            f?.let {
                val size = FileHelper.MB * 10
                val m = RandomAccessFile(it, "rw")
                    .channel
                    .map(FileChannel.MapMode.READ_WRITE, 0, size)
                var writeSize = 0
                val r = Random
                val b = ByteArray(2)
                while (writeSize < size) {
                    b[0] = (176 + r.nextInt(39)).toByte()
                    b[1] = (161 + r.nextInt(93)).toByte()
                    m.put(b)
                    writeSize += b.size
                }
                choseFile = f.toUri()
                runOnUiThread {
                    showNotice("文件已生成")
                }
            }
        }
    }

    private fun decompress() {
        if (choseFile == null) {
            showNotice("未选择文件")
            return
        }
        var f: File? = null
        try {
            f = choseFile?.toFile()
        } catch (e: Exception) {
            log(e)
        }
        if (f == null) {
            showNotice("未获取到文件")
            return
        }
        noticeHelper.loading {
            val start = System.currentTimeMillis()
            val decompress = QuickLZ.decompress(f.readBytes())
            val end = System.currentTimeMillis()
            val compressed = FileHelper.newCache("${f.name}_decompressed")
            compressed?.writeBytes(decompress) ?: return@loading
            choseFile = null
            withContext(Dispatchers.Main) {
                showNotice("解压缩完成, 耗时 ${end - start}ms")
            }
        }
    }

    private fun compress() {
        if (choseFile == null) {
            showNotice("未选择文件")
            return
        }
        var f: File? = null
        try {
            f = FileHelper.getFileFromUri(uri = choseFile!!, file = File(cacheDir, "temp"))
        } catch (e: Exception) {
            log(e)
        }
        if (f == null) {
            showNotice("未获取到文件")
            return
        }
        noticeHelper.loading {
            val start = System.currentTimeMillis()
            val compress = QuickLZ.compress(f.readBytes(), 3)
            val end = System.currentTimeMillis()
            val compressed = FileHelper.newCache("${f.name}_compressed")
            compressed?.writeBytes(compress) ?: return@loading
            choseFile = compressed.toUri()
            withContext(Dispatchers.Main) {
                val old = f.length()
                val new = compressed.length()
                showNotice("压缩完成\n大小: $old -> $new, ${((old - new).toFloat() / old.toFloat() * 100).toInt()}%\n耗时: ${end - start}ms")
            }
        }
    }
}