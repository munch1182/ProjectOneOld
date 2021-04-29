package com.munch.test.project.one.net.service

import android.content.Context
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.extend.formatDate
import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import com.yanzhenjie.andserver.annotation.*
import com.yanzhenjie.andserver.framework.config.Multipart
import com.yanzhenjie.andserver.framework.config.WebConfig
import com.yanzhenjie.andserver.framework.website.AssetsWebsite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Create by munch1182 on 2021/2/1 16:15.
 */
class AndServiceHelper private constructor() {

    companion object {
        val INSTANCE by lazy { AndServiceHelper() }

        const val PORT = 8080

    }

    private var server: Server? = null

    fun stopWebService() {
        server?.shutdown()
        server = null
    }

    fun isRunning() = server?.isRunning ?: false

    fun startWebService() {
        if (server != null) {
            if (server?.isRunning == false) {
                server?.startup()
            }
            return
        }
        server = AndServer.webServer(BaseApp.getInstance())
            .port(PORT)
            .build()
        server?.startup()
    }

    fun startOrStop() {
        if (isRunning()) stopWebService() else startWebService()
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

object Forward {
    const val INDEX = "forward:/index.html"
    const val URL = "forward:/url.html"
}

@Controller
class PageController {

    @GetMapping("/")
    fun main(): String {
        return Forward.INDEX
    }

    @GetMapping("/url")
    fun urls(): String {
        return Forward.URL
    }
}

@RestController
@RequestMapping
class AndServiceController {

    @GetMapping("/test")
    fun test(): String {
        return "yyyy-MM-dd HH:mm:ss".formatDate(System.currentTimeMillis())
    }

    @GetMapping("/test/urls")
    fun testUrls(): List<ItemLink> {
        return runBlocking(Dispatchers.IO) {
            DbHelper.getLinkDao().queryAllLink()
        }
    }

    @GetMapping("/test/types")
    fun testTypes(): List<ItemType> {
        return runBlocking(Dispatchers.IO) {
            DbHelper.getLinkDao().queryAllType()
        }
    }

    @GetMapping("/test/types/add")
    fun testAddTypes(@RequestParam("tag") tag: String) {
        runBlocking(Dispatchers.IO) {
            DbHelper.getLinkDao().addTag(tag)
        }
    }

}