package com.munch.project.one.applib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.base.startActivity
import com.munch.project.one.applib.recyclerview.RvSingleActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivity(RvSingleActivity::class.java)
    }
}