package com.munch.project.test.file

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.net.toUri
import com.munch.lib.helper.ResultHelper
import com.munch.lib.helper.getExtension
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import java.io.DataOutputStream
import java.io.File

/**
 * Create by munch1182 on 2021/1/7 14:45.
 */
class TestFileActivity : TestRvActivity() {

    companion object {

        const val ITEM_LAST = "⬆️ Parent folder"
        var itemFolder = "\uD83D\uDDC2  "
        var itemFile = "\uD83D\uDCC4  "
    }

    private var level = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ResultHelper.with(this)
                .requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .res { allGrant, _, _ ->
                    if (allGrant) {
                        open(cacheDir.parentFile)
                    } else {
                        return@res
                    }
                }
        } else {
            //兼容android11
            ResultHelper.with(this)
                .startForResult(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                .res { isOk, _, _ ->
                    if (!isOk) {
                        toast("权限拒绝")
                        return@res
                    } else {
                        open(cacheDir.parentFile)
                    }
                }
        }

        adapter.clickItemListener {
            val pos = it.tag as? Int? ?: 0
            val testRvItemBean = adapter.getData()[pos]
            val file = File(testRvItemBean.info)
            if (testRvItemBean.name == ITEM_LAST) {
                level--
                open(file.parentFile)
            } else {
                level++
                open(file)
            }
        }
    }

    private fun su(): Boolean {
        var exec: Process? = null
        var os: DataOutputStream? = null
        try {
            exec = Runtime.getRuntime().exec("su") ?: return true
            os = DataOutputStream(exec.outputStream)
            os.run {
                writeBytes("chmod 777 $packageCodePath\n")
                writeBytes("exit\n")
                flush()
                exec
            }.waitFor()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                os?.close()
                exec?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    private fun open(file: File?) {
        file?.takeIf { it.exists() } ?: return
        if (file.isFile) {
            openFile(file)
            return
        }
        val list = mutableListOf<TestRvItemBean>()
        if (level > 0) {
            list.add(TestRvItemBean(ITEM_LAST, file.absolutePath ?: ""))
        }

        file.listFiles()?.forEach {
            if (it.isDirectory) {
                list.add(TestRvItemBean("$itemFolder${it.name}", it.absolutePath))
            } else {
                list.add(TestRvItemBean("$itemFile${it.name}", it.absolutePath))
            }
        }
        adapter.setData(list.toMutableList())
    }

    private fun openFile(file: File) {
        startActivity(Intent().apply {
            action = Intent.ACTION_VIEW
            data = file.toUri().normalizeScheme()
            type = file.getExtension() ?: "text/plain"
        })
    }

    private fun cannotUse(): Boolean {
        return false
    }
}