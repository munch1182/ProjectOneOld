package com.munch.lib.imageload

import android.content.Context
import android.view.View

/**
 * Created by Munch on 2019/7/9 10:25
 */
interface ImageLoaderStrategy {

    /**
     * 加载方法
     */
    fun load(targetView: View, options: ImageLoaderOption)

    /**
     * 预加载
     * @param context 生命周期，周期结束时取消加载
     */
    fun preload(context: Context, options: ImageLoaderOption)

    /**
     * 清除资源
     */
    fun clearRes(context: Context, res: Any)
}