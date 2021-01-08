package com.munch.project.testsimple.jetpack

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.munch.lib.helper.startActivity
import com.munch.lib.test.TestBaseTopActivity


/**
 * Create by munch1182 on 2021/1/8 17:43.
 */
class WebViewActivity : TestBaseTopActivity() {

    companion object {

        private const val KEY_TITLE = "TITLE"
        private const val KEY_URL = "URL"

        fun openWebView(context: Context, title: String, url: String) {
            context.startActivity(WebViewActivity::class.java) {
                putString(KEY_TITLE, title)
                putString(KEY_URL, url)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras ?: return
        title = extras.getString(KEY_TITLE)
        val urlStr = extras.getString(KEY_URL) ?: return

        val view = WebView(this).apply {
            settings.javaScriptEnabled = true
        }
        setContentView(view)

        view.loadUrl(urlStr)
        view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: ""
                return !(url.startsWith("http") || url.startsWith("https"))
            }
        }
    }
}