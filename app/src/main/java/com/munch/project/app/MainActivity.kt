package com.munch.project.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.common.RouterHelper
import com.munch.lib.common.start2Component

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        start2Component(RouterHelper.Test.MAIN)
        finish()
    }
}