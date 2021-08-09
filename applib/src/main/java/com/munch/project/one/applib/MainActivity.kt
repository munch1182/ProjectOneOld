package com.munch.project.one.applib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.munch.lib.base.startActivity
import com.munch.lib.fast.base.bind
import com.munch.lib.fast.base.get
import com.munch.project.one.applib.databinding.ActivityMainBinding
import com.munch.project.one.applib.recyclerview.RvSingleActivity

class MainActivity : AppCompatActivity() {

    private val view by bind<ActivityMainBinding>(R.layout.activity_main)
    private val vm by get(VM::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivity(RvSingleActivity::class.java)
    }
}

class VM : ViewModel() {}