package com.munch.lib.image

import android.content.Context
import android.view.View

/**
 * Created by Munch on 2019/7/9 10:23
 */
class ImageLoaderOption {

    /**
     * 加载的图片
     */
    var res: Any? = null
    var placeHolder: Any? = null
    var error: Any? = null

    var targetWidth: Float? = null
    var targetHeight: Float? = null
    /**
     * 本地缓存
     */
    var cacheLocal: Boolean? = null
    /**
     * 为缓存签名以设置刷新
     */
    var signature: Any? = null

    /**
     * 备选参数1
     */
    var parameter1: Any? = null
    /**
     * 备选参数2
     */
    var parameter2: Any? = null
    /**
     * 备选参数3
     */
    var parameter3: Any? = null
    /**
     * 更改单次的加载策略，注意其生命周期
     */
    var strategy: ImageLoaderStrategy? = null
    /**
     * 图片ScaleType
     */
    var transformation: Any? = null

    /**
     * 缩略比
     */
    var thumbnail: Float? = null

    /**
     * 加载为gif
     */
    var isGif: Boolean = false

    var asBitmap: Boolean = false


    /**
     * 不要向外部暴露[ImageLoaderOption]，否则影响调用
     * 如需设置属性，只在[Builder]中设置方法即可
     */
    class Builder {

        private val options by lazy(mode = LazyThreadSafetyMode.NONE) { ImageLoaderOption() }

        private constructor()

        internal constructor(res: Any?) : this() {
            options.res = res
        }

        /**
         * view不应该是[ImageLoaderOption]的属性
         */
        fun into(targetView: View) {
            ImageHelper.load(targetView).into(targetView)
        }

        /**
         * 预加载
         * @param context 请求所绑定的生命周期，终止后仍未完成则会取消请求
         */
        fun preload(context: Context) {
            ImageHelper.preLoad(context, options)
        }

        fun placeHolder(placeHolder: Any?): Builder {
            options.placeHolder = placeHolder
            return this
        }

        /**
         * 设置单次加载策略
         */
        fun strategy(strategy: ImageLoaderStrategy?): Builder {
            options.strategy = strategy
            return this
        }

        fun error(error: Any?): Builder {
            options.error = error
            return this
        }

        fun res(res: Any?): Builder {
            options.res = res
            return this
        }

        fun override(width: Float?, height: Float?): Builder {
            options.targetWidth = width
            options.targetHeight = height
            return this
        }

        fun cacheLocal(cache: Boolean): Builder {
            options.cacheLocal = cache
            return this
        }

        fun signature(signature: Any): Builder {
            options.signature = signature
            return this
        }

        fun transformation(transformation: Any): Builder {
            options.transformation = transformation
            return this
        }

        fun parameter1(parameter: Any?): Builder {
            options.parameter1 = parameter
            return this
        }

        fun parameter2(parameter: Any?): Builder {
            options.parameter2 = parameter
            return this
        }

        fun parameter3(parameter: Any?): Builder {
            options.parameter3 = parameter
            return this
        }

        fun asGif(): Builder {
            options.isGif = true
            return this
        }

        fun asBitmap(): Builder {
            options.asBitmap = true
            return this
        }

        fun build() = options
    }


}