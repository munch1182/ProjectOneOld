package com.munch.test.project.one.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.munch.pre.lib.extend.obOnResume
import com.munch.pre.lib.helper.file.FileHelper
import com.munch.pre.lib.helper.file.StorageHelper
import com.munch.pre.lib.log.LogLog
import com.munch.pre.lib.log.log
import com.munch.test.project.one.base.BaseItemWithNoticeActivity
import com.munch.test.project.one.requestPermission

/**
 * Create by munch1182 on 2021/4/8 11:48.
 */
class FileChoseActivity : BaseItemWithNoticeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        obOnResume({ LogLog.setListener { msg, _ -> notice(msg) } }, { LogLog.setListener() })
    }

    private val choseFile =
        registerForActivityResult(object : ActivityResultContract<String, Uri?>() {
            override fun createIntent(context: Context, input: String): Intent {
                return StorageHelper.fileIntent(input)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                return intent?.data
            }
        }) {
            val path = FileHelper.getPathFromUri(uri = it ?: return@registerForActivityResult)
                ?: return@registerForActivityResult
            log(path)
            notice(path)
        }

    override fun clickItem(pos: Int) {
        when (pos) {
            0 -> {
                startActivity(StorageHelper.fileIntent())
            }
            1 -> {
                requestPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) {
                    log(StorageHelper.queryImages(this))
                }
            }
            2 -> {
                choseFile.launch("*/*")
            }
            3 -> {
            }
            4 -> {
            }
            else -> {
            }
        }
    }

    override fun getItem(): MutableList<String> {
        return mutableListOf("file", "image", "chose", "test")
    }
}