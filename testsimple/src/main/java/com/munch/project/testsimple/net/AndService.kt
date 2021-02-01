package com.munch.project.testsimple.net

import android.content.Context
import com.munch.lib.BaseApp
import com.munch.lib.helper.formatDate
import com.munch.lib.log
import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import com.yanzhenjie.andserver.annotation.*
import com.yanzhenjie.andserver.framework.config.Multipart
import com.yanzhenjie.andserver.framework.config.WebConfig
import com.yanzhenjie.andserver.framework.website.AssetsWebsite
import com.yanzhenjie.andserver.util.MediaType
import java.io.File

/**
 * Create by munch1182 on 2021/2/1 16:15.
 */
class AndServiceHelper private constructor() {

    companion object {
        val INSTANCE by lazy { AndServiceHelper() }
    }

    private var server: Server? = null

    fun stopWebService() {
        server?.shutdown()
        server = null
    }

    fun startWebService() {
        if (server != null) {
            if (server?.isRunning == false) {
                server?.startup()
            }
            return
        }
        server = AndServer.webServer(BaseApp.getContext())
            .port(8080)
            .build()
        server?.startup()
    }
}

@Config
class AppConfig : WebConfig {
    override fun onConfig(context: Context?, delegate: WebConfig.Delegate?) {
        delegate?.addWebsite(AssetsWebsite(context ?: return, "/web")) ?: return

        delegate.setMultipart(
            Multipart.newBuilder()
                .allFileMaxSize(1024 * 1024 * 20) // 20M
                .fileMaxSize(1024 * 1024 * 5) // 5M
                .maxInMemorySize(1024 * 10) // 1024 * 10 bytes
                .uploadTempDir(File(context!!.cacheDir, "_server_upload_cache_"))
                .build()
        )
    }
}

object Forward{
    const val INDEX = "forward:/index.html"
}

@Controller
class PageController {

    @GetMapping("/")
    fun main(): String {
        return Forward.INDEX
    }

    @PostMapping("/add/url", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun addUrl(@RequestParam("str") str: String): String {
        log(str)
        return Forward.INDEX
    }
}

@RestController
@RequestMapping
class AndServiceController {

    @GetMapping("/test")
    fun test(): String {
        return "yyyy-MM-dd HH:mm:ss".formatDate(System.currentTimeMillis())
    }


}