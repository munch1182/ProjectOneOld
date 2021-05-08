package com.munch.project.launcher.item

import android.app.Activity
import android.os.Bundle
import com.munch.pre.lib.base.Orientation
import com.munch.pre.lib.extend.startActivity
import com.munch.pre.lib.helper.SwipeViewHelper
import com.munch.pre.lib.log.log
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.databinding.ActivityAppBinding
import com.munch.project.launcher.extend.bind

/**
 * Create by munch1182 on 2021/5/8 11:43.
 */
class AppActivity : BaseActivity() {

    companion object {

        fun start(context: Activity) {
            context.startActivity(AppActivity::class.java)
            context.overridePendingTransition(R.anim.anim_enter_up, 0)
        }

    }

    private val bind by bind<ActivityAppBinding>(R.layout.activity_app)
    private val swipeHelper by lazy { SwipeViewHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        val firstAlpha = bind.appContainer.alpha
        swipeHelper.getSwipeView().orientation(Orientation.All).apply {
            processListener = {
                var processTemp = firstAlpha - it
                if (processTemp > firstAlpha) {
                    processTemp = firstAlpha
                } else if (processTemp < 0f) {
                    processTemp = 0f
                }
                bind.appContainer.alpha = processTemp
            }
            animEndListener = { if (it) onBackPressed() }
        }
        swipeHelper.setActivity()
    }

    override fun toggleTheme() {
        //不实现
        /*super.toggleTheme()*/
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.anim_exit_down)
    }
}