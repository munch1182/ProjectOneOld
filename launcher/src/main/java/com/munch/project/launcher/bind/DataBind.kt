package com.munch.project.launcher.bind

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.munch.project.launcher.help.loadCenterCrop

/**
 * Create by munch1182 on 2021/2/24 14:04.
 */
object DataBind {

    @BindingAdapter("bindImage")
    @JvmStatic
    fun bindImage(imageView: ImageView, any: Any?) {
        imageView.loadCenterCrop(any)
    }
}