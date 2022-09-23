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
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.munch.project.one.net.NetHelper
import java.io.File
import java.io.InputStream


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

fun ImageView.load(any: Any?) {
    GlideApp.with(this).let {
        when (any) {
            is String -> it.load(any)
            is File -> it.load(any)
            is Drawable -> it.load(any)
            is Uri -> it.load(any)
            is Bitmap -> it.load(any)
            is Int -> it.load(any)
            else -> throw  IllegalStateException("unSupport")
        }
    }.into(this)
}