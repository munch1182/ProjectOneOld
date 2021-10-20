package com.munch.project.one.web

import android.os.Bundle
import android.webkit.WebView
import com.munch.lib.fast.base.BaseBigTextTitleActivity

/**
 * Create by munch1182 on 2021/10/20 15:30.
 */
class WebActivity : BaseBigTextTitleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)
        val str = "<p><b>测试</b></p><p>"
        webView.loadDataWithBaseURL(null, str, "text/html", "utf-8", null)
    }
}