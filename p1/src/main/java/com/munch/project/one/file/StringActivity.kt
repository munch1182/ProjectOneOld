package com.munch.project.one.file

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.munch.lib.base.ViewHelper
import com.munch.lib.base.startActivity
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.state.ViewNoticeHelper
import com.munch.lib.weight.LoadingView
import com.munch.project.one.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Create by munch1182 on 2022/2/18 16:46.
 */
class StringActivity : BaseBigTextTitleActivity() {

    companion object {

        private const val KEY_FILE = "key_file"

        fun show(context: Context, f: String) {
            context.startActivity(StringActivity::class.java) {
                putString(KEY_FILE, f)
            }
        }
    }

    private val noticeHelper by lazy { ViewNoticeHelper(this, loading = Pair(null,
        LoadingView(this, LoadingView.STYLE_TEXT)
    )) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.setPadding(resources.getDimensionPixelSize(R.dimen.paddingDef))
        setContentView(textView, ViewHelper.newWWLayoutParams())

        intent?.getStringExtra(KEY_FILE)?.let {
            noticeHelper.showLoading()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val data = File(it).readText()
                    withContext(Dispatchers.Main) {
                        textView.text = data
                        noticeHelper.dismissLoading()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
    }
}