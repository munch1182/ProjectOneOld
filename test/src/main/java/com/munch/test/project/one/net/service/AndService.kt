package com.munch.test.project.one.net.service

import android.content.Context
import com.google.gson.Gson
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.extend.formatDate
import com.munch.pre.lib.log.log
import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import com.yanzhenjie.andserver.annotation.*
import com.yanzhenjie.andserver.framework.config.Multipart
import com.yanzhenjie.andserver.framework.config.WebConfig
import com.yanzhenjie.andserver.framework.view.ViewResolver
import com.yanzhenjie.andserver.framework.website.AssetsWebsite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.random.Random


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
            .listener(object : Server.ServerListener {
                override fun onStarted() {
                }

                override fun onStopped() {
                }

                override fun onException(e: Exception?) {
                    e?.printStackTrace()
                    log(e)
                }
            })
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

@Controller
class PageController {

    @GetMapping("/")
    fun main(): String {
        return "/index"
    }

    @GetMapping("/link")
    fun urls(): String {
        return "/link"
    }

    /**
     * [ViewResolver.resolvePath]
     *
     * 未实现只显示/tag/{tag}地址
     */
    @GetMapping("/tag/{tag}")
    fun tag(@PathVariable("tag") tag: String): String {
        return "redirect:/tag.html?tag=$tag"
    }

    @GetMapping(path = ["/tags"])
    fun tags(): String {
        return "/tags"
    }
}

/*@Converter
class BeanConverter : MessageConverter {
    override fun convert(output: Any?, mediaType: MediaType?): ResponseBody {
        if (output is ItemLink) {
            return StringBody(Gson().toJson(output), MediaType.APPLICATION_JSON_UTF8)
        }
        throw UnsupportedOperationException()
    }

    override fun <T : Any?> convert(stream: InputStream, mediaType: MediaType?, type: Type?): T? {
        val charset = mediaType?.charset
            ?: return Gson().fromJson(IOUtils.toString(stream), type)
        return Gson().fromJson(IOUtils.toString(stream, charset), type)
    }
}*/

@RequestMapping("/req")
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
class AndServiceController {

    @GetMapping("/test")
    fun test(): String {
        return "yyyy-MM-dd HH:mm:ss".formatDate(System.currentTimeMillis())
    }

    @GetMapping("/links")
    fun links(): String {
        return runBlocking(Dispatchers.IO) {
            DbHelper.getLinkDao().queryAllLink().toJson()
        }
    }

    @GetMapping("/link/add")
    fun addLink(@RequestBody links: ItemLink) {
        runBlocking(Dispatchers.IO) {
            DbHelper.getLinkDao().addLink(links)
        }
    }

    @GetMapping("/link/add/test")
    fun addLink(@RequestParam("url") string: String) {
        addLink(ItemLink(string, "test link title ${Random.nextInt(100)}", mutableListOf("test")))
    }

    @GetMapping("/queryLinks")
    fun linkByTag(@RequestParam("tag") tag: String): String {
        return runBlocking(Dispatchers.IO) {
            DbHelper.getLinkDao().queryByTag(tag).toJson()
        }
    }

    @GetMapping("/tags")
    fun tags(): String {
        return runBlocking(Dispatchers.IO) {
            DbHelper.getLinkDao().queryAllTag().toJson()
        }
    }

    @GetMapping("/tags/add")
    fun addTags(@RequestParam("tag") tag: String) {
        runBlocking(Dispatchers.IO) {
            DbHelper.getLinkDao().updateTag(tag)
        }
    }

    private fun <T : ToJson> MutableList<T>.toJson(): String {
        return Gson().toJson(this)
    }
}