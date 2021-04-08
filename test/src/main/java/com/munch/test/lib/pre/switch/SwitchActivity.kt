package com.munch.test.lib.pre.switch

import android.app.Activity
import android.os.Bundle
import com.munch.pre.lib.extend.restartApp2Activity
import com.munch.pre.lib.extend.startActivity
import com.munch.test.lib.pre.R
import com.munch.test.lib.pre.base.BaseItemActivity


/**
 * 未完成部分：主题切换渐变：
 *      思路：1. 截图并动态更改图片透明度
 *           2. 转场动画
 * Create by munch1182 on 2021/4/8 14:53.
 */
class SwitchActivity : BaseItemActivity() {

    private val instance = SwitchHelper.INSTANCE

    companion object {
        private const val KEY_RESTART = "key_restart"

        /**
         * 因为[recreate]会闪烁，所以这里新启动一个[SwitchActivity]并传入动画来让这个过程无感知
         *
         * 这种方法要通过参数传递来还原当前activity的状态以显得一模一样，例如recyclerview的滑动距离
         * 因此建议设置切换设置的界面要简单一点
         */
        fun switch(context: Activity, needRestart: Boolean = false) {
            context.startActivity(
                SwitchActivity::class.java,
                Bundle().apply {
                    putBoolean(KEY_RESTART, needRestart)
                })
            if (needRestart) {
                context.overridePendingTransition(R.anim.anim_mask_in, R.anim.anim_mask_out)
            }
        }
    }

    override fun clickItem(pos: Int) {
        when (pos) {
            0 -> instance.switchLanguage()
            1 -> instance.followSystem(!instance.isFollowSystem())
            2 -> instance.switchTheme()
            3 -> instance.switchNight()
            else -> {
            }
        }
        switch(this, true)
        finish()
    }

    override fun getItem(): MutableList<String> {
        return mutableListOf(
            "language:${instance.getNowLanguage()}",
            "follow system:${instance.isFollowSystem()}",
            "style:${instance.getThemeMode()}",
            "night mode:${instance.isNightMode()}"
        )
    }

    override fun onBackPressed() {
        //不写在[finish()]是避免切换多次直接restart
        if (intent?.extras?.getBoolean(KEY_RESTART, false) == true) {
            //这个是直接返回启动页，如果启动页面时Splash则不建议这样跳转
            // 如果启动页面是splash应该重启然后跳转主页面
            restartApp2Activity()
            //类似的动画效果可以掩盖重启感知
            overridePendingTransition(R.anim.anim_open_in, R.anim.anim_open_out)
            finish()
        } else {
            super.onBackPressed()
        }
    }
}