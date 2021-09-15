package com.munch.project.one

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.helper.BarHelper
import com.munch.lib.log.log
import kotlin.concurrent.thread
import kotlin.math.max

/**
 * Create by munch1182 on 2021/9/11 17:38.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BarHelper(this).colorStatusBar(Color.WHITE)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            return
        }
        val cost = App.getLaunchCost()
        if (cost > 500L) {
            log("启动用时:$cost ms")
        }
        thread {
            Thread.sleep(max(0L, 800L - cost))
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.anim_mask_in, R.anim.anim_mask_out)
            finish()
        }
    }
}