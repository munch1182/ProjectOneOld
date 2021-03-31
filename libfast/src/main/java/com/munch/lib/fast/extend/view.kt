package com.munch.lib.fast.extend

import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * Create by munch1182 on 2021/3/31 17:46.
 */
fun ImageView.load(any: Any?) {
    Glide.with(context).load(any).into(this)
}