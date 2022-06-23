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
import com.munch.lib.result.intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2022/5/6 17:10.
 */
class FileActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val bind by fvFv(arrayOf("new", "open"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.click { _, index ->
            when (index) {
                0 -> {

                }
                1 -> {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "*/*"
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent(Intent(intent)).start { _, data ->
                        log(data?.data)
                        lifecycleScope.launch(Dispatchers.Default) {
                            val file = FileHelper.uri2File(
                                this@FileActivity,
                                data?.data
                            ) { progress, all -> log("$progress/$all") }
                            lifecycleScope.launch(Dispatchers.Main) {
                                bind.desc(file?.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}