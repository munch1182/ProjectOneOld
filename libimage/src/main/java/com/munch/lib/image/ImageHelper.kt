package com.munch.lib.image

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import androidx.annotation.NonNull
import androidx.core.content.FileProvider
import java.io.File

/**
 * Created by Munch on 2019/7/9 10:12
 */
object ImageHelper {

    /**
     * @param authority FileProvider authority
     * @return intent 用于startActivityForResult获取结果，结果在[file]
     */
    fun takePhoto(context: Context, file: File, authority: String): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, authority, file)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            uri = Uri.fromFile(file)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        return intent
    }

    /*==================== load ====================*/

    private var strategy: ImageLoaderStrategy? = null

    @JvmStatic
    fun res(res: Any) = ImageLoaderOption.Builder(res)

    @JvmStatic
    fun clear(context: Context, res: Any) = getStrategy().clearRes(context, res)

    @JvmStatic
    fun setupStrategy(strategy: ImageLoaderStrategy) {
        this.strategy = strategy
    }

    internal fun getStrategy(): ImageLoaderStrategy {
        if (null == strategy) {
            throw RuntimeException("must call setupStrategy or config")
        }
        return strategy!!
    }

    fun loadRes(@NonNull strategy: ImageLoaderStrategy, @NonNull targetView: View, @NonNull options: ImageLoaderOption) {
        strategy.load(targetView, options)
    }

    fun preload(@NonNull strategy: ImageLoaderStrategy, @NonNull context: Context, @NonNull options: ImageLoaderOption) {
        strategy.preload(context, options)
    }

}