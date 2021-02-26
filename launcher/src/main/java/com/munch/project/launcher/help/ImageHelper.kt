package com.munch.project.launcher.help

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.munch.lib.BaseApp

/**
 * Create by munch1182 on 2021/2/26 14:54.
 */
fun ImageView.load(any: Any?) {
    Glide.with(this).load(any).into(this)
}

fun Drawable.preload() {
    Glide.with(BaseApp.getContext()).load(this).preload()
}