package com.munch.project.testsimple.com.munch.project.testsimple

import android.os.Bundle
import com.munch.project.testsimple.MainRvActivity
import com.munch.project.testsimple.R

class MainRvAloneActivity : MainRvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.test_simple_app_name)
        showBack(false)
    }
}