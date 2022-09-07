package com.munch.project.one

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.munch.project.one.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        val str = "${BuildConfig.BUILD_TYPE}\n" +
                "${BuildConfig.APPLICATION_ID}\n" +
                "${BuildConfig.VERSION_CODE}\n" +
                "${BuildConfig.VERSION_NAME}\n" +
                "${BuildConfig.VERSION_DESC}\n" +
                BuildConfig.BUILD_TIME
        vb.text.text = str
    }
}
