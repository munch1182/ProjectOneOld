package com.munch.test.project.one.file

import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.lib.fast.extend.get
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.log.log
import com.munch.pre.lib.extend.observeOnChanged
import com.munch.pre.lib.extend.toLiveData
import com.munch.pre.lib.helper.file.FileHelper
import com.munch.pre.lib.helper.file.closeQuietly
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityFileCopyBinding
import com.munch.test.project.one.requestPermission
import kotlinx.coroutines.*
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Create by munch1182 on 2021/4/13 13:37.
 */
class FileCopyActivity : BaseTopActivity() {

    private val bind by bind<ActivityFileCopyBinding>(R.layout.activity_file_copy)
    private val mode by get(FileCopyViewModel::class.java)
    private var type = 0

    private val requestFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        mode.start(it, type)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@FileCopyActivity
            fileCopyStart.setOnClickListener { requestWithType(FileCopyViewModel.TYPE_3) }
            fileCopyStart1.setOnClickListener { requestWithType(FileCopyViewModel.TYPE_1) }
            fileCopyStart2.setOnClickListener { requestWithType(FileCopyViewModel.TYPE_2) }
            fileCopyStop.setOnClickListener { mode.stop() }
        }
        mode.getProgress().observeOnChanged(this) {
            bind.fileCopyStart1Tv.text = it
        }
        mode.getProgress2().observeOnChanged(this) {
            bind.fileCopyStart2Tv.text = it
        }
    }

    private fun requestWithType(type: Int) {
        this.type = type
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
            requestFile.launch(arrayOf("*/*"))
        }
    }

    internal class FileCopyViewModel : ViewModel() {

        companion object {

            internal const val TYPE_1 = 1
            internal const val TYPE_2 = 2
            internal const val TYPE_3 = 3
        }

        private val progressStr = MutableLiveData("")
        private val progressStr2 = MutableLiveData("")
        fun getProgress() = progressStr.toLiveData()
        fun getProgress2() = progressStr.toLiveData()

        fun start(it: Uri, type: Int) {
            when (type) {
                TYPE_1 -> {
                    CopyThread(it) { c, a ->
                        progressStr.postValue("$c/$a (${((c.toDouble() / a.toDouble()) * 100.0).toInt()}%)")
                    }.start()
                }
                TYPE_2 -> {
                    CopyThread2(it) { c, a ->
                        progressStr2.postValue("$c/$a (${((c.toDouble() / a.toDouble()) * 100.0).toInt()}%)")
                    }.start()
                }
                TYPE_3 -> {
                    start(it, TYPE_1)
                    start(it, TYPE_2)
                    viewModelScope.launch(Dispatchers.IO) {
                        while (running1) {
                            delay(220L)
                            synchronized(lock1) {
                                lock1.notify()
                            }
                        }
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        while (running2) {
                            delay(220L)
                            synchronized(lock2) {
                                receiver2 = true
                            }
                        }
                    }
                }
            }
        }

        fun stop() {
            synchronized(lock1) {
                running1 = false
                running2 = false
            }
        }

        private val lock1 = Object()
        private val lock2 = Object()
        private var running1 = true
        private var running2 = true
        private var receiver2 = false

        private inner class CopyThread2(
            private val uri: Uri,
            private val listener: (cur: Long, all: Long) -> Unit
        ) : Thread() {

            override fun run() {
                super.run()
                val cache = FileHelper.cacheFileOrNew(name = "test2")
                var num = 0
                var ins: InputStream? = null
                var os: FileOutputStream? = null
                try {
                    ins = BaseApp.getInstance().contentResolver.openInputStream(uri)
                    val byte = ByteArray(480 + 5)
                    os = FileOutputStream(cache)
                    var length = ins!!.read(byte)
                    var allRead = 0
                    val available = ins.available()
                    log("start2: start: $available")
                    while (length > 0) {

                        os.write(byte)
                        allRead += length
                        log("start2: $num : $allRead")
                        while (true) {
                            sleep(10L)
                            if (synchronized(lock2) { receiver2 }) {
                                break
                            }
                        }
                        synchronized(lock2) {
                            if (receiver2) {
                                receiver2 = false
                            }
                        }
                        length = ins.read(byte)
                        listener.invoke(allRead.toLong(), available.toLong())
                        num++
                    }
                } catch (e: Exception) {
                    log("start2: $e")
                } finally {
                    ins?.closeQuietly()
                    os.closeQuietly()
                    log("start2: end")
                }
                synchronized(lock1) {
                    running1 = false
                }
            }
        }

        private inner class CopyThread(
            private val uri: Uri,
            private val listener: (cur: Long, all: Long) -> Unit
        ) : Thread() {

            override fun run() {
                super.run()

                val cache = FileHelper.cacheFileOrNew(name = "test")
                var num = 0
                var ins: InputStream? = null
                var os: FileOutputStream? = null
                try {
                    ins = BaseApp.getInstance().contentResolver.openInputStream(uri)
                    val byte = ByteArray(480 + 5)
                    os = FileOutputStream(cache)
                    var length = ins!!.read(byte)
                    var allRead = 0
                    val available = ins.available()
                    log("start1: $available")
                    while (length > 0) {

                        os.write(byte)
                        allRead += length
                        synchronized(lock1) {
                            val start = System.currentTimeMillis()
                            log("start1: $num wait")
                            lock1.wait(10000L)
                            log("start1: $num notify: ${System.currentTimeMillis() - start}")
                            if (!running1) {
                                return
                            }
                        }
                        length = ins.read(byte)
                        listener.invoke(allRead.toLong(), available.toLong())
                        num++
                    }
                } catch (e: Exception) {
                    log("start1: $e")
                } finally {
                    ins?.closeQuietly()
                    os.closeQuietly()
                    log("start1: end")
                }
                synchronized(lock1) {
                    running1 = false
                }
            }
        }

    }
}