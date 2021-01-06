package com.munch.project.test.switch

import android.app.Activity
import android.os.Bundle
import com.munch.lib.helper.startActivity
import com.munch.project.test.BaseActivity
import com.munch.project.test.R

/**
 * 一个透明activity
 * Create by munch1182 on 2020/12/30 13:37.
 */
class MaskActivity : BaseActivity() {

    companion object {

        fun showMask(context: Activity) {
            context.startActivity(MaskActivity::class.java)
            context.overridePendingTransition(R.anim.anim_mask_in, R.anim.anim_mask_out)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        window.decorView.postDelayed({
            finish()
        }, 400L)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.anim_mask_out)
    }
}