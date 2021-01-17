package com.munch.project.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.common.RouterHelper
import com.munch.lib.common.start2Component

/**
 * 此类作为壳引用启动页，主要用于xml注册，是Splash的占位
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        start2Component(RouterHelper.Test.MAIN)
        /*overridePendingTransition(R.anim.anim_mask_in, R.anim.anim_mask_in)*/
        finish()
    }
}