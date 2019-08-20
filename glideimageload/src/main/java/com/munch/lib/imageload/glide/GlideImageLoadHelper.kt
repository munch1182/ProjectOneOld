package com.munch.lib.imageload.glide

import android.content.Context
import android.view.View
import androidx.annotation.NonNull
import com.munch.lib.imageload.ImageLoadHelper
import com.munch.lib.imageload.ImageLoaderOption

/**
 * Created by Munch on 2019/8/16 17:51
 */
object GlideImageLoadHelper {

    @JvmStatic
    fun res(res: Any) {
        checkStrategy()
        ImageLoadHelper.res(res)
    }

    private fun checkStrategy() {
        if (ImageLoadHelper.getStrategy() == null) {
            ImageLoadHelper.setupStrategy(getGlideStrategy())
        }
    }

    private fun getGlideStrategy() = GlideStrategy()

    @JvmStatic
    fun clear(context: Context, res: Any) {
        checkStrategy()
        ImageLoadHelper.clear(context, res)
    }

    @JvmStatic
    fun loadRes(@NonNull targetView: View, @NonNull options: ImageLoaderOption) {
        ImageLoadHelper.loadRes(getGlideStrategy(), targetView, options)
    }

    @JvmStatic
    fun preload(@NonNull context: Context, @NonNull options: ImageLoaderOption) {
        ImageLoadHelper.preload(getGlideStrategy(), context, options)
    }

}