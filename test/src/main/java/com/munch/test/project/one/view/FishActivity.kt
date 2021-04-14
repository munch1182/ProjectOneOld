package com.munch.test.project.one.view

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import com.munch.lib.fast.base.activity.BaseActivity
import com.munch.lib.fast.weight.FishDrawableWithStructure
import com.munch.pre.lib.helper.BarHelper

/**
 * Create by munch1182 on 2021/4/14 10:34.
 */
class FishActivity : BaseActivity() {

    private var draw = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BarHelper(this).hideStatusBar(true).colorStatusBar(Color.TRANSPARENT)
        setContentView(ImageView(this).apply {
            val fish = FishDrawableWithStructure()
            setImageDrawable(fish)
            setOnClickListener {
                draw = !draw
                fish.drawStructure(draw)
            }
        })

    }
}