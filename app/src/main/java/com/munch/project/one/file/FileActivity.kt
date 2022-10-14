package com.munch.project.one.file

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.munch.lib.android.extend.bind
import com.munch.lib.android.extend.lazy
import com.munch.lib.android.helper.FileHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.newRandomString
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.ActivityFileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Create by munch1182 on 2022/10/14 11:11.
 */
class FileActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private val bind by bind<ActivityFileBinding>()
    private val sb = StringBuilder()
    private var content = ""
    private val file by lazy { FileHelper.mmap(FileHelper.new("text_mmap.txt")) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.fileGenerate.setOnClickListener { generateContent() }
        bind.fileWrite.setOnClickListener { write() }
        bind.fileRead.setOnClickListener { read() }
    }

    private fun read() {
        lifecycleScope.launch {
            flow { emit(file.readText()) }
                .flowOn(Dispatchers.IO)
                .collect { bind.fileContentFromFile.text = it }
        }
    }

    private fun write() {
        if (content.isEmpty()) return
        lifecycleScope.launch(Dispatchers.IO) { file.write("${content}\n") }
        updateDesc("内容已追加到文件${file.absoluteFile}中(${file.length()})")
    }


    private fun generateContent() {
        content = newRandomString(len = Random.nextInt(5) * 30 + 600, sb)
        showContent()
    }

    private fun showContent() {
        bind.fileContent.text = content
    }

    private fun updateDesc(desc: String) {
        bind.fileDesc.text = desc
    }

    override fun onDestroy() {
        super<BaseActivity>.onDestroy()
        file.close()
    }
}