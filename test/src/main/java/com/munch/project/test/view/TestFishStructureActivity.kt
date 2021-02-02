package com.munch.project.test.view

import android.os.Bundle
import android.widget.ImageView
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.test.R

/**
 * Create by munch1182 on 2021/2/2 11:20.
 */
class TestFishStructureActivity : TestBaseTopActivity() {

    private val img: ImageView by lazy { findViewById(R.id.fish_img) }
    private var draw = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity_fish_structure)
        val fish = FishDrawableWithStructure()
        img.setImageDrawable(fish)
        img.setOnClickListener {
            draw = !draw
            fish.drawStructure(draw)
        }
    }

}