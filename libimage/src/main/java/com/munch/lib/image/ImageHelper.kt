package com.munch.lib.image

import android.content.Context
import java.lang.RuntimeException

/**
 * Created by Munch on 2019/7/9 10:12
 */
object ImageHelper {

    private var strategy: ImageLoaderStrategy? = null

    @JvmStatic
    fun load(res: Any) = ImageLoaderOption.Builder(res)

    @JvmStatic
    fun preLoad(context: Context, res: Any) = getStrategy().preload(context, ImageLoaderOption.Builder(res).build())

    @JvmStatic
    fun clear() = getStrategy().clearRes()

    @JvmStatic
    fun setupStrategy(strategy: ImageLoaderStrategy) {
        this.strategy = strategy
    }

    private fun getStrategy(): ImageLoaderStrategy {
        if (null == strategy) {
            throw RuntimeException("must call setupStrategy or config")
        }
        return strategy!!
    }

}