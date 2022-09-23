package com.munch.project.one.other

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.impInMain
import com.munch.project.one.net.NetHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.InputStream
import kotlin.coroutines.resume


/**
 * 让Glide使用指定的OkHttp对象
 *
 * Create by munch1182 on 2022/9/22 17:40.
 */
@GlideModule
class OkhttpGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(NetHelper.client)
        )
    }
}

object ImageHelper {
    fun ImageView.load(any: Any?) {
        if (any == null) {
            setImageDrawable(null)
            return
        }
        val request = GlideApp.with(this).let {
            when (any) {
                is String -> it.load(any)
                is File -> it.load(any)
                is Drawable -> it.load(any)
                is Uri -> it.load(any)
                is Bitmap -> it.load(any)
                is Int -> it.load(any)
                else -> throw  IllegalStateException("unSupport")
            }
        }
        impInMain { request.into(this) }
    }

    /**
     * 将[url]的图片下载到文件并返回
     */
    suspend fun down(url: String): File? = suspendCancellableCoroutine {
        GlideApp.with(AppHelper)
            .downloadOnly()
            .load(url)
            .listener(object : RequestListener<File> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<File>?,
                    isFirstResource: Boolean
                ): Boolean {
                    it.resume(null)
                    return true
                }

                override fun onResourceReady(
                    resource: File?,
                    model: Any?,
                    target: Target<File>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    it.resume(resource)
                    return true
                }
            })
            .preload()
    }
}