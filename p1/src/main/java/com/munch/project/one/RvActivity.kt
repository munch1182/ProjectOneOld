package com.munch.project.one

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Create by munch1182 on 2022/3/31 22:39.
 */
class RvActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextDrawDiagramView(this).apply {
        })

    }
}