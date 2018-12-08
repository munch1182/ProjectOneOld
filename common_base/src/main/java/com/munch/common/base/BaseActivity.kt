package com.munch.common.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by Munch on 2018/12/8.
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView(savedInstanceState)
        initData(savedInstanceState)
    }

    open fun initData(bundle: Bundle?) {

    }

    open fun initView(bundle: Bundle?) {
    }
}