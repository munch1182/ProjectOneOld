package com.munch.project.one.net

import android.content.Context
import androidx.annotation.IntRange
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.annotations.SerializedName
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.catch
import com.munch.lib.android.extend.fmt
import com.munch.lib.android.log.LogInfo
import com.munch.lib.android.log.Logger
import com.munch.project.one.other.ImageHelper
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.io.File

/**
 * Create by munch1182 on 2022/9/23 11:17.
 */
interface BiYing {

    companion object {
        const val baseUrl = "https://cn.bing.com"

        val curr: File
            get() = File(
                AppHelper.cacheDir,
                "biYing/${System.currentTimeMillis().fmt("yyyyMMdd")}.png"
            )
    }

    @Headers("${NetHelper.HEADER}: $baseUrl")
    @GET("/HPImageArchive.aspx")
    suspend fun wallpaper(
        @Query("n") number: Int = 1, // 返回数量
        @Query("idx") @IntRange(from = 0, to = 7) offset: Int = 0, // 今天及之前7天某一天的数据
        @Query("format") format: String = "js" // json: js xml:xml
    ): BiYingWallpaper
}

data class BiYingWallpaper(val images: List<BiYingImage>?) : NetResultBean<List<BiYingImage>?> {
    override val result: NetResult
        get() = images?.let { NetResult.Success } ?: NetResult.Fail(null)
    override val data: List<BiYingImage>?
        get() = images

    val url: String?
        get() = data?.get(0)?.url?.let { "${BiYing.baseUrl}$it" }
}

data class BiYingImage(
    @SerializedName("fullstartdate")
    val date: String,
    val url: String,
    val urlBase: String,
    val copyright: String,
    val copyrightLink: String,
    val title: String,
    val hsh: String,
)

class BiYingWork(context: Context, parameter: WorkerParameters) :
    CoroutineWorker(context, parameter) {

    private val log = Logger("BY", LogInfo.Thread)

    override suspend fun doWork(): Result {
        log.log("work start.")
        val currFile = BiYing.curr
        if (!currFile.exists()) {
            log.log("request today wallpaper.")
            /*val size = "${InfoHelper.screenWidth}x${InfoHelper.screenHeight}"*/
            val url = NetHelper.img.wallpaper().url/*?.replaceFirst("1920x1080", size)*/
            if (url == null) {
                log.log("fail to request and finish work.")
            } else {
                log.log("success to request, save wallpaper to file.")
                val save = catch { ImageHelper.down(url)?.copyTo(currFile, true) }
                log.log("save file ${if (save != null) "success" else "fail"}.")
            }
        } else {
            log.log("wallpaper file exit, ignore network.")
        }
        log.log("work complete.")
        return Result.success()
    }
}