package com.munch.lib.imageload.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.request.target.Target
import com.munch.lib.imageload.ImageLoaderOption
import com.munch.lib.imageload.ImageLoaderStrategy

/**
 * Created by Munch on 2019/7/13 9:32
 */
class GlideStrategy : ImageLoaderStrategy {

    override fun load(targetView: View, options: ImageLoaderOption) {
        setupRes(targetView.context, options).into(targetView as ImageView)
    }

    override fun preload(context: Context, options: ImageLoaderOption) {
        setupRes(context, options)
            .preload(
                options.targetWidth?.toInt() ?: Target.SIZE_ORIGINAL,
                options.targetHeight?.toInt() ?: Target.SIZE_ORIGINAL
            )
    }

    private fun setupRes(context: Context, options: ImageLoaderOption): RequestBuilder<*> {
        var manager: RequestBuilder<*>
        manager = when {
            options.isGif -> Glide.with(context).asGif().load(options.res)
            options.asBitmap -> Glide.with(context).asBitmap().load(options.res)
            options.asFile -> Glide.with(context).asFile().load(options.res)
            else -> Glide.with(context).asDrawable().load(options.res)
        }
        if (options.targetWidth != null && options.targetHeight != null) {
            manager = manager.override(options.targetWidth!!.toInt(), options.targetHeight!!.toInt())
        }
        if (options.placeHolder is Drawable) {
            manager = manager.placeholder(options.placeHolder as Drawable)
        } else if (options.placeHolder is Int) {
            manager = manager.placeholder(options.placeHolder as Int)
        }

        if (options.error is Drawable) {
            manager = manager.placeholder(options.error as Drawable)
        } else if (options.error is Int) {
            manager = manager.placeholder(options.error as Int)
        }

        if (options.signature is Key) {
            manager = manager.signature(options.signature as Key)
        }

        @Suppress("UNCHECKED_CAST")
        if (options.transformations is Array<*>) {
            val array = options.transformations
            if (array != null) {
                manager = if (array.size == 1) {
                    manager.transform(array as Transformation<Bitmap>)
                } else {
                    manager.transform(*(array as Array<out Transformation<Bitmap>>))
                }
            }
        }

        if (options.thumbnail != null) {
            manager = manager.thumbnail(options.thumbnail!!)
        }

        return manager
    }

    override fun clearRes(context: Context, res: Any) {
        if (res is View) {
            Glide.with(context).clear(res)
        } else if (res is Target<*>) {
            Glide.with(context).clear(res)
        }
    }
}