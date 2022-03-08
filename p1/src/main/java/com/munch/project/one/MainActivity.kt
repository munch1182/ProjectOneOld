package com.munch.project.one

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.munch.project.one.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        vb.text.text = "${BuildConfig.BUILD_TYPE}\n${BuildConfig.APPLICATION_ID}\n${BuildConfig.VERSION_CODE}\n${BuildConfig.VERSION_NAME}\n${BuildConfig.VERSION_DESC}\n${BuildConfig.BUILD_TIME}"
    }
}