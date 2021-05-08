package com.munch.project.launcher.extend

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

/**
 * Create by munch1182 on 2021/3/31 17:46.
 */
fun ImageView.load(any: Any?) {
    Glide.with(context).load(any).into(this)
}

@BindingAdapter("load")
fun loadFast(imageView: ImageView, any: Any?) {
    imageView.load(any)
}