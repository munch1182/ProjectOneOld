package com.munch.lib.imageload

import android.content.Context
import android.view.View
import androidx.annotation.NonNull

/**
 * Created by Munch on 2019/7/9 10:12
 */
object ImageLoadHelper {

    private var strategy: ImageLoaderStrategy? = null

    fun getStrategy() = strategy

    @JvmStatic
    fun res(res: Any) = ImageLoaderOption.Builder(res)

    @JvmStatic
    fun clear(context: Context, res: Any) = checkStrategy().clearRes(context, res)

    @JvmStatic
    fun setupStrategy(strategy: ImageLoaderStrategy) {
        this.strategy = strategy
    }

    internal fun checkStrategy(): ImageLoaderStrategy {
        if (null == strategy) {
            throw RuntimeException("must call setupStrategy or config")
        }
        return strategy!!
    }

    @JvmStatic
    fun loadRes(@NonNull strategy: ImageLoaderStrategy, @NonNull targetView: View, @NonNull options: ImageLoaderOption) {
        strategy.load(targetView, options)
    }

    @JvmStatic
    fun preload(@NonNull strategy: ImageLoaderStrategy, @NonNull context: Context, @NonNull options: ImageLoaderOption) {
        strategy.preload(context, options)
    }

}