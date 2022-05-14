package com.munch.project.one.file

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.fvFv
import com.munch.lib.fast.view.supportDef
import com.munch.lib.helper.FileHelper
import com.munch.lib.log.log
import com.munch.lib.result.OnIntentResultListener
import com.munch.lib.result.ResultHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2022/5/6 17:10.
 */
class FileActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val bind by fvFv(arrayOf("open"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.click { _, index ->
            when (index) {
                0 -> {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "*/*"
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    ResultHelper.with(this)
                        .intent(Intent(intent))
                        .start(object : OnIntentResultListener {
                            override fun onIntentResult(
                                isOk: Boolean,
                                resultCode: Int,
                                data: Intent?
                            ) {
                                log(data?.data)
                                lifecycleScope.launch(Dispatchers.Default) {
                                    log(
                                        FileHelper.uri2File(
                                            this@FileActivity,
                                            data?.data,
                                            onProgress = object : FileHelper.OnProgressListener {
                                                override fun onProgress(progress: Long, all: Long) {
                                                    log("$progress/$all")
                                                }
                                            })
                                    )
                                }
                            }
                        })
                }
            }
        }
    }
}