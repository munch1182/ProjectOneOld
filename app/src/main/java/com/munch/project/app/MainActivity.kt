package com.munch.project.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.munch.lib.common.RouterHelper
import com.munch.lib.common.start2Component

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        start2Component(RouterHelper.Test.MAIN)
        finish()
    }
}