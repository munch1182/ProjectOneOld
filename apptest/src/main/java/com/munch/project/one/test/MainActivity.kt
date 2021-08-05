package com.munch.project.one.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.munch.lib.fast.FastAppHelper
import com.munch.project.one.test.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bind =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val sb = StringBuilder()
        FastAppHelper.collectPhoneInfo().forEach {
            sb.append("${it.key}:${it.value ?: "null"}\r\n")
        }
        bind.testMain.text = sb.toString()
    }
}