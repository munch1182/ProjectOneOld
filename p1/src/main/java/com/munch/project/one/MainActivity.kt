package com.munch.project.one

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.munch.lib.android.extend.bind
import com.munch.project.one.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val vb by bind<ActivityMainBinding>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vb.text.text = "${BuildConfig.BUILD_TYPE}\n${BuildConfig.APPLICATION_ID}\n" +
                "${BuildConfig.VERSION_CODE}\n${BuildConfig.VERSION_NAME}\n" +
                "${BuildConfig.VERSION_DESC}\n${BuildConfig.BUILD_TIME}\n" +
                "${Build.VERSION.SDK_INT}"
    }
}