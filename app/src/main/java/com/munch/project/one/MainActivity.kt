package com.munch.project.one

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.munch.project.one.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)
    }

}
