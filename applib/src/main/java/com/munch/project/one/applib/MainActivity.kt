package com.munch.project.one.applib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.munch.lib.base.startActivity
import com.munch.project.one.applib.recyclerview.RvMultiActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivity(RvMultiActivity::class.java)
    }
}